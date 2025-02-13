import { api } from './baseApi';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { showToast } from '../utility/handleToast';

export const useFeedbackReceived = (userId, params) => {
  return useQuery({
    queryKey: ['feedback-received', userId, params],
    retry: 0,
    queryFn: () => {
      let sendingParams = {
        filterHelpful: params.onlyLiked,
        sortOrder: params.sortBy === 'createdAt:desc' ? 'DESC' : 'ASC',
        page: params.page,
      };
      if (params.teamId) {
        sendingParams.teamId = params.teamId;
      }
      return api.get({
        url: `/api/feedbacks/receiver/${userId}`,
        params: sendingParams,
      });
    },
  });
};

export const useFeedbackSent = (userId, params) => {
  return useQuery({
    queryKey: ['feedback-sent', userId, params],
    retry: 0,
    queryFn: () => {
      let sendingParams = {
        filterHelpful: params.onlyLiked,
        sortOrder: params.sortBy === 'createdAt:desc' ? 'DESC' : 'ASC',
        page: params.page,
      };
      if (params.teamId) {
        sendingParams.teamId = params.teamId;
      }
      return api.get({
        url: `/api/feedbacks/sender/${userId}`,
        params: sendingParams,
      });
    },
  });
};

export const useFeedbackLike = (userId, feedbackId) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () =>
      api.post({ url: `/api/member/${userId}/feedbacks/${feedbackId}/liked` }),
    onSuccess: (data) => {
      showToast(data.message);
      queryClient.invalidateQueries({ queryKey: ['feedback-received'] });
    },
    onError: (error) => {
      showToast(error.response.data.message);
    },
  });
};

export const useFeedbackLikeCancel = (userId, feedbackId) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () =>
      api.delete({
        url: `/api/member/${userId}/feedbacks/${feedbackId}/liked`,
      }),
    onSuccess: (data) => {
      showToast(data.message);
      queryClient.invalidateQueries({ queryKey: ['feedback-received'] });
    },
    onError: (error) => {
      showToast(error.response.data.message);
    },
  });
};

export const useFeedbackFavorite = () => {
  return useQuery({
    queryKey: ['feedback-preference'],
    queryFn: () => api.get({ url: '/api/feedback/preference' }),
  });
};

export const useEditFavorite = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data) =>
      api.post({ url: '/api/member/feedback-prefer', body: data }),
    onSuccess: () => {
      // TODO: 사용자 선호 피드백 정보 조회할때 캐시를 지우도록 수정
      // queryClient.invalidateQueries({ queryKey: ['feedback-favorite'] });
    },
  });
};

export const useFeedbackFavoriteByUser = () => {
  return useQuery({
    queryKey: ['feedback-favorite-by-user'],
    queryFn: (data) =>
      api.get({ url: `/api/member/feedback-prefer?findMemberId=${data}` }),
  });
};

export const useFeedbackObjective = () => {
  return useQuery({
    queryKey: ['feedback-objective'],
    queryFn: () => api.get({ url: '/api/feedback/objective' }),
  });
};

export const useFeedbackRefinement = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({ url: '/api/feedback-refinement', body: data }),
  });
};
