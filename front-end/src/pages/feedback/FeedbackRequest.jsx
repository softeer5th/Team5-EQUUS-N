import NavBar2 from '../../components/NavBar2';
import StickyWrapper from '../../components/wrappers/StickyWrapper';
import TextArea from '../../components/TextArea';
import LargeButton from '../../components/buttons/LargeButton';
import FooterWrapper from '../../components/wrappers/FooterWrapper';
import { useState } from 'react';
import { showToast } from '../../utility/handleToast';
import { useFeedbackRequest } from '../../api/useFeedback2';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTeam } from '../../useTeam';

export default function FeedbackRequest() {
  const [textLength, setTextLength] = useState(0);
  const [textContent, setTextContent] = useState('');

  const location = useLocation();
  const navigate = useNavigate();

  const queryParams = new URLSearchParams(location.search);
  const receiverId = queryParams.get('receiverId');
  const receiverName = queryParams.get('receiverName');

  const mutation = useFeedbackRequest();
  const { selectedTeam } = useTeam();

  return (
    <div className='flex size-full flex-col'>
      <StickyWrapper>
        <NavBar2
          canPop={true}
          onClickPop={() => {
            navigate('/main');
          }}
        />
        <h1 className='header-2 text-gray-0 mt-6 whitespace-pre-line'>
          {`${receiverName}님에게 요청할\n피드백을 작성해주세요`}
        </h1>
      </StickyWrapper>
      <div className='h-6' />
      <TextArea
        textContent={textContent}
        textLength={textLength}
        setTextLength={setTextLength}
        setTextContent={setTextContent}
      />
      <FooterWrapper>
        <LargeButton
          isOutlined={false}
          text={mutation.isPending ? '로딩중' : '보내기'} // 로딩 중일 때 버튼 텍스트 변경... 추후 수정 필요
          disabled={textLength === 0 ? true : false}
          onClick={() => {
            setTextContent((prev) => prev.trim());
            if (textContent.trim().length === 0)
              showToast('내용을 입력해주세요');
            else if (textLength > 400) showToast('400자 이하로 작성해주세요');
            else
              mutation.mutate(
                {
                  receiverId: receiverId,
                  teamId: selectedTeam,
                  requestedContent: textContent.trim(),
                },
                {
                  onSuccess: () =>
                    navigate(`/feedback/complete/?type=${'REQUEST'}`, {
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
