import NavBar from './components/NavBar';
import CustomInput from '../../components/CustomInput';
import LargeButton from '../../components/buttons/LargeButton';
import Icon from '../../components/Icon';
import { useState } from 'react';

export default function SignIn() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  return (
    <div className='relative flex h-screen w-full flex-col justify-start'>
      <NavBar title='로그인' />
      <div className='h-2' />
      <CustomInput
        label='이메일'
        hint='이메일을 입력해 주세요'
        content={email}
        setContent={setEmail}
        type='email'
      />
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
      <div className='absolute right-0 bottom-[34px] left-0 h-20 bg-gray-900'>
        <LargeButton text='로그인하기' isOutlined={false} />
      </div>
    </div>
  );
}
