import Lottie from 'lottie-react';
import logoLottie from '../../assets/lotties/logo.json';
import { useLocation, useNavigate } from 'react-router-dom';
import { useGoogleLogin } from '../../api/useAuth';
import { useEffect } from 'react';
import { useJoinTeam } from '../../api/useTeamspace';
import Modal from '../../components/modals/Modal';
import MediumButton from '../../components/buttons/MediumButton';
import { hideModal } from '../../utility/handleModal';

/**
 * 스플래시 페이지 - 소셜로그인할때 code 받아와서 서버에 전송하는 동안 머뭄
 * @returns
 */
export default function SplashForOAuth() {
  const location = useLocation();
  const searchParams = new URLSearchParams(location.search);
  const code = searchParams.get('code'); // Google OAuth 인증 코드
  const navigate = useNavigate();
  const teamCode = localStorage.getItem('tempTeamCode');
  const { mutate: googleLogin } = useGoogleLogin(teamCode, errorModal);

  useEffect(() => {
    const timerId = setTimeout(() => {
      hideModal();
      navigate('/signin', { replace: true });
    }, 3500);

    return () => clearTimeout(timerId);
  }, []);

  useEffect(() => {
    if (code) {
      googleLogin(code);
    }
  }, [code, googleLogin]);

  return (
    <div className='flex h-full w-full items-center justify-center'>
      <div className='flex h-[20%] w-[20%] items-center justify-center'>
        <Lottie animationData={logoLottie} loop={true} autoplay={true} />
      </div>
    </div>
  );
}

const errorModal = (
  <Modal
    type='SINGLE'
    title={
      <p className='text-center leading-loose'>
        이미 이메일로 가입하신 적이 있어요!
        <br />
        이메일 로그인으로 이동할게요.
      </p>
    }
    mainButton={
      <MediumButton
        text='확인'
        isOutlined={false}
        onClick={() => {
          navigate('/signin', { replace: true });
        }}
      />
    }
  />
);
