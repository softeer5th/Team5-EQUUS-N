import { useLocation, useNavigate } from 'react-router-dom';
import {
  useFeedbackFavoriteByUser,
  useFeedbackRefinement,
} from '../../../api/useFeedback';
import Tag, { TagType } from '../../../components/Tag';
import TextArea from '../../../components/TextArea';
import { useTeam } from '../../../useTeam';
import { useReducer, useState } from 'react';
import AiButton from '../../../components/buttons/AiButton';
import FooterWrapper from '../../../components/wrappers/FooterWrapper';
import LargeButton from '../../../components/buttons/LargeButton';
import { transformToBytes } from '../../../utility/inputChecker';
import {
  useFrequnetFeedbackSend,
  useRegularFeedbackSend,
} from '../../../api/useFeedback2';
import { showToast } from '../../../utility/handleToast';
import { hideModal, showModal } from '../../../utility/handleModal';
import Modal, { ModalType } from '../../../components/modals/Modal';
import MediumButton from '../../../components/buttons/MediumButton';

export default function FeedbackSend3() {
  const navigate = useNavigate();
  const locationState = useLocation().state;

  const { data: favoriteKeywords } = useFeedbackFavoriteByUser(
    locationState.receiver.id,
  );
  const { teams, selectedTeam } = useTeam();
  const gptMutation = useFeedbackRefinement();
  const feedbackMutation =
    locationState.isRegular ?
      useRegularFeedbackSend()
    : useFrequnetFeedbackSend();

  const [isAnonymous, toggleAnonymous] = useReducer(
    (prev) => !prev,
    teams.find((team) => team.id === selectedTeam).feedbackType === 'ANONYMOUS',
  );
  const [textLength, setTextLength] = useState(0);
  const [textContent, setTextContent] = useState('');
  const [gptContents, setGptContents] = useState({
    index: null,
    contents: [],
  });

  const generateGptContent = () => {
    const trimmedContent = textContent.trim();
    const trimmedContentLength = transformToBytes(trimmedContent).byteCount;
    setTextContent(trimmedContent);
    setTextLength(trimmedContentLength);

    if (trimmedContentLength < 10) {
      showToast('내용을 10byte 이상 작성해 주세요');
      return;
    }
    if (trimmedContentLength > 400) {
      showToast('내용을 400byte 이하로 작성해 주세요');
      return;
    }

    gptMutation.mutate(
      {
        receiverId: locationState.receiver.id,
        objectiveFeedbacks: locationState.objectiveFeedback,
        subjectiveFeedback: textContent,
      },
      {
        onSuccess: (data) => {
          setGptContents((prev) => {
            return {
              index: prev.contents.length,
              contents: [...prev.contents, data.subjectiveFeedback],
            };
          });
        },
        onError: (error) => {
          if (error.status === 429)
            showModal(
              <Modal
                type={ModalType.CUSTOM}
                title='AI 사용 횟수 초과'
                content={`AI 사용 횟수 3회를 초과하였습니다.\n피드백을 전송하면 초기화됩니다.`}
                mainButton={
                  <MediumButton
                    isOutlined={false}
                    text='확인'
                    onClick={hideModal}
                  />
                }
              />,
            );
        },
      },
    );
  };

  const onSendButtonClick = () => {
    const trimmedContent = textContent.trim();
    const trimmedContentLength = transformToBytes(trimmedContent).byteCount;
    setTextContent(trimmedContent);
    setTextLength(trimmedContentLength);
    if (trimmedContentLength === 0) {
      showToast('내용을 입력해 주세요');
      return;
    }
    if (trimmedContentLength > 400) {
      showToast('내용을 400byte 이하로 작성해 주세요');
      return;
    }
    const { receiver, isRegular, ...rest } = locationState;
    feedbackMutation.mutate(
      {
        ...rest,
        receiverId: locationState.receiver.id,
        subjectiveFeedback: textContent,
        isAnonymous,
        teamId: selectedTeam,
      },
      { onSuccess: () => navigate('../../complete?type=SEND') },
    );
  };

  const gptValidation = () => {
    if (gptMutation.isIdle) return false;
    if (gptMutation.isError && gptContents.index === null) return false;
    return true;
  };

  return (
    <div className='flex w-full flex-col pb-28'>
      <h1 className='header-2 text-gray-0 mt-3 whitespace-pre-line'>
        {'자세한 내용을 작성해 보세요!'}
      </h1>
      <p className='body-1 mt-8 mb-2 text-gray-300'>{`${locationState.receiver.name}님이 원하는 피드백 스타일이에요!`}</p>
      {favoriteKeywords && (
        <div className='mb-5 flex flex-wrap gap-2'>
          {favoriteKeywords.feedbackPreferences.map((keyword, index) => (
            <Tag key={index} type={TagType.KEYWORD}>
              {keyword}
            </Tag>
          ))}
        </div>
      )}
      <TextArea
        textContent={textContent}
        textLength={textLength}
        setTextContent={setTextContent}
        setTextLength={setTextLength}
        isWithGpt={true}
        canToggleAnonymous={true}
        toggleAnonymous={toggleAnonymous}
        isAnonymous={isAnonymous}
      />
      {gptValidation() && (
        <>
          <div className='h-5' />
          <TextArea
            gptContents={gptContents}
            generatedByGpt={true}
            isGptLoading={gptMutation.isPending}
            setGptContents={setGptContents}
          />
        </>
      )}
      <div className='h-5' />
      <div className='flex w-full justify-end'>
        {gptValidation() ?
          <div className='flex items-center gap-2'>
            <AiButton
              isActive={false}
              onClick={() =>
                setTextContent(gptContents.contents[gptContents.index])
              }
            >
              적용하기
            </AiButton>
            <AiButton isActive={true} onClick={() => generateGptContent()}>
              재생성하기
            </AiButton>
          </div>
        : !gptMutation.isPending && (
            <AiButton isActive={true} onClick={() => generateGptContent()}>
              AI 글 다듬기
            </AiButton>
          )
        }
      </div>
      <FooterWrapper>
        <LargeButton
          isOutlined={false}
          text={feedbackMutation.isPending ? '로딩중' : '다음'}
          disabled={textLength === 0}
          onClick={() => onSendButtonClick()}
        />
      </FooterWrapper>
    </div>
  );
}
