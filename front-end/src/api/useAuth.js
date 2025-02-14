import { api } from './baseApi';
import { useMutation, useQuery } from '@tanstack/react-query';
import { replace, useNavigate } from 'react-router-dom';
import { showToast } from '../utility/handleToast';
import { useUser } from '../useUser';
import { useJoinTeam } from './useTeamspace';
import { useTeam } from '../useTeam';

const BASE_URL_2 = '/api/auth';

export const useSendVerifMail = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({
        url: BASE_URL_2 + '/send-signup-verification-email',
        body: data,
      }),
  });
};

export const useVerifyToken = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({ url: BASE_URL_2 + '/verify-signup-email-token', body: data }),
  });
};

export const useGetMember = (id) => {
  return useQuery({
    queryKey: ['member', id],
    queryFn: () => api.get({ url: '/api/member', params: { id } }),
  });
};

export const useSignUp = () => {
  return useMutation({
    mutationFn: (data) =>
      api.post({ url: '/api/auth/email/signup', body: data }),
    onSuccess: () => {},
  });
};

export const useLogin = (teamCode) => {
  const navigate = useNavigate();
  const { setUserId } = useUser();
  const { mutate: joinTeam } = useJoinTeam();
  return useMutation({
    mutationFn: (data) =>
      api.post({ url: '/api/auth/email/login', body: data }),
    onSuccess: (data) => {
      const { email, message, userId } = data;
      setUserId(userId);
      if (teamCode) {
        joinTeam(teamCode);
      }
      navigate('/main');
    },
    onError: (error) => {
      showToast(`로그인 실패: ${error.message}`);
    },
  });
};

export const useLogout = () => {
  const navigate = useNavigate();
  const { setTeams } = useTeam();
  const { setUserId } = useUser();
  return useMutation({
    mutationFn: () => api.post({ url: '/api/auth/logout' }),
    onSuccess: () => {
      setUserId(null);
      setTeams([]);
      navigate('/', { replace: true });
    },
  });
};
