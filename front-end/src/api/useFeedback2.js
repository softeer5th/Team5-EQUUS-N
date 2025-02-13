import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from './baseApi';

export const useFeedbackRequest = () => {
  // const queryClient = useQueryClient();
  return useMutation({
    /**
     * 피드백 요청 전송
     * @param {object} props
     * @param {number} props.receiverId - 피드백을 받을 사람의 ID
     * @param {number} props.teamId - 팀 ID
     * @param {string} props.requestedContent - 요청 내용
     * @returns
     */
    mutationFn: ({ receiverId, teamId, requestedContent }) =>
      api.post({
        url: `/api/feedbacks/frequent/request`,
        body: {
          receiverId,
          teamId,
          requestedContent,
        },
      }),
    onSuccess: (data) => {
      // queryClient.invalidateQueries({ queryKey: ['feedback-sent'] }); // 추후 고민...
    },
    onError: (error) => {
      console.error('전송실패', error);
    },
  });
};

export const useFeedbackSelf = () => {
  // const queryClient = useQueryClient();
  return useMutation({
    /**
     * 피드백 요청 전송
     * @param {object} props
     * @param {number} props.writerId - 작성자 본인의 ID
     * @param {number} props.teamId - 팀 ID
     * @param {string} props.title - 제목
     * @param {string} props.content - 내용
     * @returns
     */
    mutationFn: ({ writerId, teamId, title, content }) =>
      api.post({
        url: `/api/retrospect`,
        body: {
          writerId,
          teamId,
          title,
          content,
        },
      }),
    onSuccess: (data) => {
      // queryClient.invalidateQueries({ queryKey: ['feedback-sent'] }); // 추후 고민...
    },
    onError: (error) => {
      console.error('전송실패', error);
    },
  });
};

export const useRegularFeedbackSend = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({
        url: '/api/feedbacks/regular',
        body: data,
      }),
  });
};

export const useFrequnetFeedbackSend = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({
        url: '/api/feedbacks/frequent',
        body: data,
      }),
  });
};
