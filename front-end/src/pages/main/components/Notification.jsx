import classNames from 'classnames';
import Lottie from 'lottie-react';
import boxLottie from '../../../assets/lotties/box.json';
import fileLottie from '../../../assets/lotties/file.json';
import letterLottie from '../../../assets/lotties/letter.json';
import Icon from '../../../components/Icon';

export const notiType = Object.freeze({
  REQUEST: 'frequentFeedbackRequest',
  REPORT: 'feedbackReportCreate',
  UNREAD: 'unreadFeedbackExist',
  NEW: 'feedbackReceive',
});

/**
 * 노티파이 컴포넌트
 * @param {Object} props
 * @param {object} props.notification
 * @param {Function} props.onClick
 * @param {Function} props.onClose
 * @param {Array<number>} props.feedbackRequestNotiIds
 */
export default function Notification({
  notification,
  onClick,
  onClose,
  feedbackRequestNotiIds,
}) {
  // TODO: 파동 애니메이션 추가
  // TODO: delete 아이콘, 화살표 아이콘 수정
  const getContent = () => {
    switch (notification.type) {
      case notiType.UNREAD:
        return {
          animationData: letterLottie,
          message: '확인하지 않은\n피드백이 있어요!',
          buttonText: '피드백 확인하기',
        };
      case notiType.NEW:
        return {
          animationData: letterLottie,
          message: '새로운 피드백이\n도착했어요!',
          buttonText: '피드백 확인하기',
        };
      case notiType.REPORT:
        return {
          animationData: fileLottie,
          message: `${notification.receiverName}님의 피드백을\n정리했어요!`,
          buttonText: '피드백 리포트 확인하기',
        };
      case notiType.REQUEST:
        return {
          animationData: boxLottie,
          message:
            feedbackRequestNotiIds.length > 1 ?
              `${notification.senderName}님 외 ${feedbackRequestNotiIds.length - 1}명이\n피드백을 요청했어요!`
            : `${notification.senderName}님이\n피드백을 요청했어요!`,
          buttonText: '피드백 보내기',
        };
      default:
        throw new Error('Invalid notiType');
    }
  };

  const { animationData, message, buttonText } = getContent();

  return (
    <div
      className={classNames(
        'rounded-400 relative w-full',
        (
          notification.type === notiType.REQUEST ||
            notification.type === notiType.UNREAD
        ) ?
          'bg-gray-100'
        : 'bg-lime-500',
      )}
    >
      <Lottie animationData={animationData} />

      <p className='header-4 absolute top-5 left-6 whitespace-pre-line'>
        {message}
      </p>

      <button
        className='absolute bottom-4 left-6 flex items-center gap-0.5'
        onClick={onClick}
      >
        <p className='caption-2 text-gray-600'>{buttonText}</p>
        <Icon name='chevronDown' className='-rotate-90' />
      </button>

      <button
        className='absolute top-4 right-4'
        onClick={() =>
          notification.type === notiType.REQUEST ?
            onClose({ notificationIds: feedbackRequestNotiIds })
          : onClose({ notificationIds: [notification.notificationId] })
        }
      >
        <Icon name='delete' color='var(--color-gray-500)' />
      </button>
      <div className='absolute right-[14.6%] bottom-2 h-[30px] w-[24%] rounded-tl-[7px] rounded-tr-[7px] bg-gradient-to-b from-[#2a2a2a] from-60% to-transparent'>
        <p className='mt-1 text-center text-[10px] font-thin text-gray-100'>
          NEW
        </p>
      </div>
    </div>
  );
}
