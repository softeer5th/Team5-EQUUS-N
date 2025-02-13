import NavBar from './components/NavBar';
import CustomInput from '../../components/CustomInput';
import LargeButton from '../../components/buttons/LargeButton';
import logo from '../../assets/images/logo.png';
import Icon from '../../components/Icon';
import { useState } from 'react';
import { useLogin } from '../../api/useAuth';
import { useLocation } from 'react-router-dom';

/**
 * 로그인 페이지
 * @returns
 */
export default function SignIn() {
  const locationState = useLocation().state;
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const { mutate: login, isLoading } = useLogin(locationState);

  return (
    <div className='relative flex h-dvh w-full flex-col justify-start'>
      <NavBar
        title={
          <p className='flex items-center justify-between'>
            로그인
            <img src={logo} className='w-8' />
          </p>
        }
      />
      <div className='h-2' />
      {/* 이메일 입력 */}
      <CustomInput
        label='이메일'
        hint='이메일을 입력해 주세요'
        content={email}
        setContent={setEmail}
        type='email'
      />
      {/* 비밀번호 입력 */}
      <div className='h-6' />
      <CustomInput
        label='비밀번호'
        hint='영문, 숫자 포함 8글자 이상'
        content={password}
        setContent={setPassword}
        isPassword={!isPasswordVisible}
        addOn={
          <button onClick={() => setIsPasswordVisible(!isPasswordVisible)}>
            <Icon
              name='eye'
              color={
                !isPasswordVisible ?
                  'var(--color-gray-500)'
                : 'var(--color-lime-500)'
              }
            />
          </button>
        }
      />
      {/* 로그인 버튼 */}
      <div className='absolute right-0 bottom-[34px] left-0 bg-gray-900'>
        <LargeButton
          text='로그인하기'
          isOutlined={false}
          onClick={() => login({ email: email, password: password })}
          disabled={isLoading} // 로딩 중일 때 버튼 비활성화
        />
      </div>
    </div>
  );
}
