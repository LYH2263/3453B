package com.club.controller;

import com.club.common.Result;
import com.club.entity.Vote;
import com.club.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    @PostMapping
    @PreAuthorize("hasRole('CLUB_LEADER')")
    public Result<?> createVote(@RequestBody Map<String, Object> body) {
        Vote vote = new Vote();
        vote.setTitle((String) body.get("title"));
        vote.setType((String) body.get("type"));
        String deadlineStr = (String) body.get("deadline");
        if (deadlineStr != null && deadlineStr.length() > 19) {
            deadlineStr = deadlineStr.substring(0, 19);
        }
        vote.setDeadline(java.time.LocalDateTime.parse(deadlineStr));
        if (body.containsKey("maxChoices")) {
            Object mc = body.get("maxChoices");
            if (mc instanceof Number) {
                vote.setMaxChoices(((Number) mc).intValue());
            }
        }
        @SuppressWarnings("unchecked")
        List<String> options = (List<String>) body.get("options");
        return voteService.createVote(vote, options);
    }

    @GetMapping
    public Result<?> getVotes(@RequestParam(required = false) Integer clubId) {
        return voteService.getVotes(clubId);
    }

    @GetMapping("/{id}")
    public Result<?> getVoteDetail(@PathVariable Integer id) {
        return voteService.getVoteDetail(id);
    }

    @PostMapping("/{id}/ballot")
    public Result<?> submitBallot(@PathVariable Integer id, @RequestBody Map<String, List<Integer>> body) {
        return voteService.submitBallot(id, body.get("optionIds"));
    }

    @GetMapping("/{id}/result")
    public Result<?> getVoteResult(@PathVariable Integer id) {
        return voteService.getVoteResult(id);
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('CLUB_LEADER')")
    public Result<?> closeVote(@PathVariable Integer id) {
        return voteService.closeVote(id);
    }
}
