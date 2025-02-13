import { useLocation, useNavigate } from 'react-router-dom';
import { ProfileImageWithText } from '../../components/ProfileImage';
import FooterWrapper from '../../components/wrappers/FooterWrapper';

export default function FeedbackSend() {
  const navigate = useNavigate();
  const locationState = useLocation().state;

  /** @type {Member[]} */
  const members = locationState.members;

  return (
    <div className='flex size-full flex-col gap-8'>
      <h1 className='header-2 text-gray-0 mt-3 whitespace-pre-line'>
        {'피드백을 보낼 팀원을\n선택해 주세요.'}
      </h1>
      {members.length > 0 ?
        <div className='flex flex-wrap gap-4'>
          {members.map((member, index) => (
            <ProfileImageWithText
              key={index}
              text={member.name}
              iconName={`@animals/${member.profileImage.image}`}
              color={member.profileImage.backgroundColor}
              onClick={() =>
                navigate('1', {
                  state: {
                    receiver: { name: member.name, id: member.id },
                    scheduleId: locationState.scheduleId,
                    isRegular: locationState.isRegular,
                  },
                })
              }
            />
          ))}
        </div>
      : <p className='mx-auto text-gray-300'>팀원이 존재하지 않아요</p>}
      <FooterWrapper>
        <div className='mb-4 flex w-full justify-center'>
          <button
            className='button-2 text-gray-300 underline underline-offset-4'
            onClick={() => console.log('낄낄')}
          >
            이번 피드백 건너뛰기
          </button>
        </div>
      </FooterWrapper>
    </div>
  );
}
