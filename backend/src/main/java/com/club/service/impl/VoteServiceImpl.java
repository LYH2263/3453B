package com.club.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.club.common.Result;
import com.club.common.RoleConstants;
import com.club.entity.User;
import com.club.entity.Vote;
import com.club.entity.VoteBallot;
import com.club.entity.VoteOption;
import com.club.mapper.UserMapper;
import com.club.mapper.VoteBallotMapper;
import com.club.mapper.VoteMapper;
import com.club.mapper.VoteOptionMapper;
import com.club.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VoteOptionMapper voteOptionMapper;

    @Autowired
    private VoteBallotMapper voteBallotMapper;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, auth.getName()));
    }

    private void checkAndCloseExpired(Vote vote) {
        if ("OPEN".equals(vote.getStatus()) && vote.getDeadline() != null && LocalDateTime.now().isAfter(vote.getDeadline())) {
            vote.setStatus("CLOSED");
            this.updateById(vote);
        }
    }

    @Override
    @Transactional
    public Result<?> createVote(Vote vote, List<String> options) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");
        if (!RoleConstants.CLUB_LEADER.equals(user.getRole())) return Result.error("仅社团负责人可创建投票");
        if (user.getClubId() == null) return Result.error("未绑定社团");
        if (options == null || options.size() < 2) return Result.error("至少需要2个选项");

        vote.setClubId(user.getClubId());
        vote.setCreatorId(user.getId());
        vote.setStatus("OPEN");
        if (vote.getMaxChoices() == null || vote.getMaxChoices() < 1) {
            vote.setMaxChoices("MULTIPLE".equals(vote.getType()) ? options.size() : 1);
        }
        if ("SINGLE".equals(vote.getType())) {
            vote.setMaxChoices(1);
        }
        this.save(vote);

        for (int i = 0; i < options.size(); i++) {
            VoteOption option = new VoteOption();
            option.setVoteId(vote.getId());
            option.setOptionText(options.get(i));
            option.setVoteCount(0);
            option.setSortOrder(i);
            voteOptionMapper.insert(option);
        }

        return Result.success(null);
    }

    @Override
    public Result<?> getVotes(Integer clubId) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        LambdaQueryWrapper<Vote> wrapper = new LambdaQueryWrapper<Vote>()
                .orderByDesc(Vote::getCreateTime);
        if (clubId != null) {
            wrapper.eq(Vote::getClubId, clubId);
        }

        List<Vote> votes = this.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Vote v : votes) {
            checkAndCloseExpired(v);
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("title", v.getTitle());
            map.put("type", v.getType());
            map.put("maxChoices", v.getMaxChoices());
            map.put("deadline", v.getDeadline());
            map.put("status", v.getStatus());
            map.put("clubId", v.getClubId());
            map.put("createTime", v.getCreateTime());

            long ballotCount = voteBallotMapper.selectCount(new LambdaQueryWrapper<VoteBallot>()
                    .eq(VoteBallot::getVoteId, v.getId())
                    .eq(VoteBallot::getUserId, user.getId()));
            map.put("hasVoted", ballotCount > 0);

            result.add(map);
        }
        return Result.success(result);
    }

    @Override
    public Result<?> getVoteDetail(Integer id) {
        Vote vote = this.getById(id);
        if (vote == null) return Result.error("投票不存在");

        checkAndCloseExpired(vote);

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", vote.getId());
        detail.put("title", vote.getTitle());
        detail.put("type", vote.getType());
        detail.put("maxChoices", vote.getMaxChoices());
        detail.put("deadline", vote.getDeadline());
        detail.put("status", vote.getStatus());
        detail.put("clubId", vote.getClubId());
        detail.put("createTime", vote.getCreateTime());

        List<VoteOption> options = voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>()
                        .eq(VoteOption::getVoteId, id)
                        .orderByAsc(VoteOption::getSortOrder));
        detail.put("options", options);

        User user = getCurrentUser();
        if (user != null) {
            List<VoteBallot> myBallots = voteBallotMapper.selectList(new LambdaQueryWrapper<VoteBallot>()
                    .eq(VoteBallot::getVoteId, id)
                    .eq(VoteBallot::getUserId, user.getId()));
            List<Integer> votedOptionIds = myBallots.stream()
                    .map(VoteBallot::getOptionId)
                    .collect(Collectors.toList());
            detail.put("votedOptionIds", votedOptionIds);
        }

        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<?> submitBallot(Integer voteId, List<Integer> optionIds) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");

        Vote vote = this.getById(voteId);
        if (vote == null) return Result.error("投票不存在");

        checkAndCloseExpired(vote);

        if ("CLOSED".equals(vote.getStatus())) return Result.error("投票已截止");
        if (!user.getClubId().equals(vote.getClubId())) return Result.error("非本社团成员无法投票");
        if (optionIds == null || optionIds.isEmpty()) return Result.error("请选择选项");

        long existingBallots = voteBallotMapper.selectCount(new LambdaQueryWrapper<VoteBallot>()
                .eq(VoteBallot::getVoteId, voteId)
                .eq(VoteBallot::getUserId, user.getId()));
        if (existingBallots > 0) return Result.error("您已投过票");

        if ("SINGLE".equals(vote.getType()) && optionIds.size() > 1) {
            return Result.error("单选投票只能选一个选项");
        }
        if ("MULTIPLE".equals(vote.getType()) && optionIds.size() > vote.getMaxChoices()) {
            return Result.error("多选投票最多选择" + vote.getMaxChoices() + "个选项");
        }

        List<VoteOption> allOptions = voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>().eq(VoteOption::getVoteId, voteId));
        Set<Integer> validOptionIds = allOptions.stream()
                .map(VoteOption::getId)
                .collect(Collectors.toSet());
        for (Integer oid : optionIds) {
            if (!validOptionIds.contains(oid)) return Result.error("选项不存在");
        }

        for (Integer optionId : optionIds) {
            VoteBallot ballot = new VoteBallot();
            ballot.setVoteId(voteId);
            ballot.setUserId(user.getId());
            ballot.setOptionId(optionId);
            voteBallotMapper.insert(ballot);

            VoteOption option = voteOptionMapper.selectById(optionId);
            if (option != null) {
                option.setVoteCount(option.getVoteCount() + 1);
                voteOptionMapper.updateById(option);
            }
        }

        return Result.success(null);
    }

    @Override
    public Result<?> getVoteResult(Integer id) {
        Vote vote = this.getById(id);
        if (vote == null) return Result.error("投票不存在");

        checkAndCloseExpired(vote);

        List<VoteOption> options = voteOptionMapper.selectList(
                new LambdaQueryWrapper<VoteOption>()
                        .eq(VoteOption::getVoteId, id)
                        .orderByAsc(VoteOption::getSortOrder));

        int totalVotes = options.stream().mapToInt(VoteOption::getVoteCount).sum();

        List<Map<String, Object>> optionResults = new ArrayList<>();
        for (VoteOption opt : options) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", opt.getId());
            m.put("optionText", opt.getOptionText());
            m.put("voteCount", opt.getVoteCount());
            m.put("percentage", totalVotes > 0
                    ? String.format("%.1f", opt.getVoteCount() * 100.0 / totalVotes)
                    : "0.0");
            optionResults.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", vote.getId());
        result.put("title", vote.getTitle());
        result.put("type", vote.getType());
        result.put("status", vote.getStatus());
        result.put("totalVotes", totalVotes);
        result.put("options", optionResults);

        return Result.success(result);
    }

    @Override
    public Result<?> closeVote(Integer id) {
        User user = getCurrentUser();
        if (user == null) return Result.error("未认证");
        if (!RoleConstants.CLUB_LEADER.equals(user.getRole())) return Result.error("仅社团负责人可关闭投票");

        Vote vote = this.getById(id);
        if (vote == null) return Result.error("投票不存在");
        if (!user.getClubId().equals(vote.getClubId())) return Result.error("只能关闭本社团的投票");

        vote.setStatus("CLOSED");
        this.updateById(vote);
        return Result.success(null);
    }
}
