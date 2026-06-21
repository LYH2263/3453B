package com.club.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.club.common.Result;
import com.club.entity.Vote;
import java.util.List;
import java.util.Map;

public interface VoteService extends IService<Vote> {
    Result<?> createVote(Vote vote, List<String> options);
    Result<?> getVotes(Integer clubId);
    Result<?> getVoteDetail(Integer id);
    Result<?> submitBallot(Integer voteId, List<Integer> optionIds);
    Result<?> getVoteResult(Integer id);
    Result<?> closeVote(Integer id);
}
