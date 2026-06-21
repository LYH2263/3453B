import request from '../utils/request'

export interface VoteOption {
  id: number
  voteId: number
  optionText: string
  voteCount: number
  sortOrder: number
}

export interface Vote {
  id: number
  title: string
  type: 'SINGLE' | 'MULTIPLE'
  maxChoices: number
  deadline: string
  status: 'OPEN' | 'CLOSED'
  clubId: number
  createTime: string
  hasVoted: boolean
}

export interface VoteDetail {
  id: number
  title: string
  type: 'SINGLE' | 'MULTIPLE'
  maxChoices: number
  deadline: string
  status: 'OPEN' | 'CLOSED'
  clubId: number
  createTime: string
  options: VoteOption[]
  votedOptionIds: number[]
}

export interface VoteOptionResult {
  id: number
  optionText: string
  voteCount: number
  percentage: string
}

export interface VoteResult {
  id: number
  title: string
  type: 'SINGLE' | 'MULTIPLE'
  status: 'OPEN' | 'CLOSED'
  totalVotes: number
  options: VoteOptionResult[]
}

const voteApi = {
  createVote: (data: { title: string; type: string; deadline: string; maxChoices?: number; options: string[] }) =>
    request.post('/votes', data),

  getVotes: (clubId?: number) =>
    request.get<Vote[]>('/votes', { params: clubId ? { clubId } : {} }),

  getVoteDetail: (id: number) =>
    request.get<VoteDetail>(`/votes/${id}`),

  submitBallot: (id: number, optionIds: number[]) =>
    request.post(`/votes/${id}/ballot`, { optionIds }),

  getVoteResult: (id: number) =>
    request.get<VoteResult>(`/votes/${id}/result`),

  closeVote: (id: number) =>
    request.post(`/votes/${id}/close`)
}

export default voteApi
