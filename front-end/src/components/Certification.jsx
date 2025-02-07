import { useEffect, useRef, useState } from 'react';
import CustomInput from './CustomInput';
import SmallButton from './buttons/SmallButton';
import Icon from './Icon';
import { showToast } from '../utility/handleToast';

export const CertState = Object.freeze({
  BEFORE_SEND_CODE: '인증코드 전송',
  RESEND_CODE: '재전송',
  AFTER_SEND_CODE: '인증하기',
  AFTER_CHECK_CODE: '인증 완료',
});

/**
 * 인증 컴포넌트
 * @param {object} props
 * @param {string} props.email - 이메일
 * @returns
 */
export default function Certification({ email = '', certState, setCertState }) {
  const [certCode, setCertCode] = useState('');
  const dueTime = 300; // 5분
  const [restTime, setRestTime] = useState(dueTime);
  const timerRef = useRef(null);
  const inputRef = useRef(null);
  const [shouldFocus, setShouldFocus] = useState(false);

  // 제한시간 설정
  const setTimer = () => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
    }
    setRestTime(dueTime);
    const timer = setInterval(() => {
      setRestTime((prev) => {
        if (prev === 0) {
          clearInterval(timer);
          setCertState(CertState.RESEND_CODE);
          return prev;
        }
        return prev - 1;
      });
    }, 1000);
    timerRef.current = timer;
  };

  // 인증코드 4자리, 제한시간 이내인 경우 인증하기 버튼 활성화
  useEffect(() => {
    if (certCode.length > 4) {
      setCertCode(certCode.slice(0, 4));
    }
    if (
      certState === CertState.RESEND_CODE &&
      restTime <= dueTime &&
      restTime > 0 &&
      certCode.length === 4
    ) {
      setCertState(CertState.AFTER_SEND_CODE);
    } else if (
      certState === CertState.AFTER_SEND_CODE &&
      certCode.length !== 4
    ) {
      setCertState(CertState.RESEND_CODE);
    }
  }, [certCode]);

  // 상태가 변경될 때마다 focus 처리
  useEffect(() => {
    if (shouldFocus) {
      inputRef.current?.focus();
      setShouldFocus(false);
    }
  }, [shouldFocus]);

  return (
    <div className='flex gap-3'>
      <div className='flex-1'>
        <CustomInput
          ref={inputRef}
          hint={'인증코드 4자리'}
          keyboardType='numeric'
          content={certCode}
          setContent={
            certState !== CertState.BEFORE_SEND_CODE &&
            certState !== CertState.AFTER_CHECK_CODE &&
            setCertCode
          }
          addOn={
            certState === CertState.AFTER_CHECK_CODE ?
              <Icon name='checkBoxClick' color='var(--color-lime-500)' />
            : <p className='text-right text-lime-500'>
                {timerRef.current ?
                  `${Math.floor(restTime / 60)
                    .toString()
                    .padStart(2, '0')}:${(restTime % 60)
                    .toString()
                    .padStart(2, '0')}`
                : ''}
              </p>
          }
          isPassword={certState === CertState.AFTER_CHECK_CODE}
        />
      </div>
      <SmallButton
        text={certState}
        disabled={
          (certState === CertState.BEFORE_SEND_CODE && email === '') ||
          certState === CertState.AFTER_CHECK_CODE
        }
        onClick={() => {
          if (certState === CertState.BEFORE_SEND_CODE) {
            // 이메일 검증
            // 이미 가입되어 있으면 버튼 비활성화

            // 인증코드 전송 절차
            setCertState(CertState.RESEND_CODE);
            showToast('인증번호를 전송했습니다');
            setTimer();
            setShouldFocus(true);
          } else if (certState === CertState.RESEND_CODE) {
            setCertCode('');
            // TODO: 인증코드 재전송 절차
            showToast('인증번호를 재전송했습니다');
            setTimer();
            setShouldFocus(true);
          } else if (certState === CertState.AFTER_SEND_CODE) {
            // TODO: 인증코드 확인 절차

            // 틀림
            // TODO: 팝업 띄우기
            // setCertCode('');
            // setCertState(CertState.BEFORE_SEND_CODE);

            // 맞음
            clearInterval(timerRef.current);
            setCertState(CertState.AFTER_CHECK_CODE);
            showToast('이메일 인증 완료');
          }
        }}
        isOutlined={false}
      />
    </div>
  );
}
