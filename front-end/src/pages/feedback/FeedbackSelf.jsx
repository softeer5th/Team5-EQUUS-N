import NavBar2 from '../../components/NavBar2';
import StickyWrapper from '../../components/wrappers/StickyWrapper';
import TextArea from '../../components/TextArea';
import LargeButton from '../../components/buttons/LargeButton';
import FooterWrapper from '../../components/wrappers/FooterWrapper';
import { useState } from 'react';
import { showToast } from '../../utility/handleToast';
import { useFeedbackSelf } from '../../api/useFeedback2';
import { useNavigate } from 'react-router-dom';
import CustomInput from '../../components/CustomInput';
import { useUser } from '../../useUser';
import { useTeam } from '../../useTeam';

export default function FeedbackSelf() {
  const [titleContent, setTitleContent] = useState('');
  const [textLength, setTextLength] = useState(0);
  const [textContent, setTextContent] = useState('');

  const navigate = useNavigate();

  const { userId } = useUser();
  const { selectedTeam } = useTeam();
  const mutation = useFeedbackSelf();

  return (
    <div className='flex size-full flex-col'>
      <StickyWrapper>
        <NavBar2
          canPop={true}
          title='회고 작성하기'
          onClickPop={() => {
            navigate('/main');
          }}
        />
      </StickyWrapper>
      <div className='h-6' />
      <CustomInput
        content={titleContent}
        setContent={setTitleContent}
        isForRetrospect={true}
        hint='제목을 입력해주세요'
      />
      <div className='h-6' />
      <TextArea
        textLength={textLength}
        setTextLength={setTextLength}
        setTextContent={setTextContent}
      />
      <FooterWrapper>
        <LargeButton
          isOutlined={false}
          text={mutation.isPending ? '로딩중' : '완료'} // 로딩 중일 때 버튼 텍스트 변경... 추후 수정 필요
          disabled={
            textLength === 0 || titleContent.length === 0 ? true : false
          }
          onClick={() => {
            if (textLength === 0) showToast('내용을 입력해주세요');
            else if (textLength > 400) showToast('400자 이하로 작성해주세요');
            else
              mutation.mutate(
                {
                  writerId: userId,
                  teamId: selectedTeam,
                  title: titleContent,
                  content: textContent,
                },
                {
                  onSuccess: () =>
                    navigate(`/feedback/complete/?type=${'RETROSPECT'}`, {
                      replace: true,
                    }),
                },
              );
          }}
        />
      </FooterWrapper>
    </div>
  );
}
