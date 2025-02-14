import { useNavigate } from 'react-router-dom';
import NavBar2 from '../../components/NavBar2';
import ProfileImage from '../../components/ProfileImage';
import { useSearchMember } from '../../api/useMyPage';
import { useUser } from '../../useUser';
import Icon from '../../components/Icon';
import { useId } from 'react';
import { useLogout } from '../../api/useAuth';

export default function MyPageHome() {
  const navigate = useNavigate();
  const { userId } = useUser();
  const { data: member } = useSearchMember(userId);
  const { mutate: logout } = useLogout();

  const listButtontexts = [
    ['피드백 리포트', 'report'],
    ['팀 스페이스 관리', '/teamspace/list'],
    ['피드백 선호도 관리', '/feedback/favorite'],
    ['나의 회고', 'self'],
  ];

  const handleLogout = () => {
    logout();
  };

  return (
    <div className='flex size-full flex-col'>
      <NavBar2 canClose={true} onClickClose={() => navigate('/main')} />
      <div className='mx-auto flex flex-col items-center'>
        {member && (
          <>
            <div className='size-20'>
              <ProfileImage
                iconName={`@animals/${member.profileImage.image}`}
                color={member.profileImage.backgroundColor}
              />
            </div>
            <h1 className='header-2 text-gray-0 mt-4 text-center'>
              {member.name}
            </h1>
            <button
              className='caption-1 mt-1 text-gray-200 underline underline-offset-1'
              onClick={() => navigate('edit')}
            >
              프로필 수정
            </button>
          </>
        )}
      </div>
      <div className='rounded-400 mt-10 flex w-full justify-evenly bg-gray-700 py-5'>
        <FeedbackCount
          isSent={true}
          count={20} // api 연결 후 수정
          onClick={() => navigate('/feedback/sent')}
        />
        <div className='h-full w-px bg-gray-500' />
        <FeedbackCount
          count={40} // api 연결 후 수정
          onClick={() => navigate('/feedback/received')}
        />
      </div>
      <ul className='mt-8 flex w-full flex-col'>
        {listButtontexts.map((item, index) => (
          <ListButton
            text={item[0]}
            onClick={() => navigate(item[1])}
            key={index}
          />
        ))}
      </ul>
      <div className='flex-1' />
      <a
        className='body-1 mb-[50px] text-center text-gray-400'
        onClick={() => handleLogout()}
      >
        로그아웃
      </a>
    </div>
  );
}

const FeedbackCount = ({ count, isSent, onClick }) => {
  return (
    <button className='flex flex-col items-center' onClick={onClick}>
      <p className='header-1 mx-4 text-white'>{count}</p>
      <p className='caption-1 mx-4 text-gray-300'>
        {isSent ? '보낸 피드백' : '받은 피드백'}
      </p>
    </button>
  );
};

const ListButton = ({ text, onClick }) => {
  return (
    <li
      className='subtitle-3 flex cursor-pointer items-center justify-between px-5 py-3 text-white transition duration-200 active:bg-gray-700'
      onClick={onClick}
    >
      <p>{text}</p>
      <Icon name='chevronRight' />
    </li>
  );
};
