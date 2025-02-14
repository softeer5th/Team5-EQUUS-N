import { useEffect, useReducer, useRef, useState } from 'react';
import {
  useMainCard,
  useMainCard2,
  useNotification,
} from '../../api/useMainPage';
import Accordion from '../../components/Accordion';
import MainCard2 from '../../components/MainCard2';
import StickyWrapper from '../../components/wrappers/StickyWrapper';
import MainCard from './components/MainCard';
import Notification from './components/Notification';
import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import '../../slider.css';
import { filterNotifications } from '../../utility/handleNotification';
import { useLocation, useNavigate } from 'react-router-dom';
import { hideModal, showModal } from '../../utility/handleModal';
import Modal, { ModalType } from '../../components/modals/Modal';
import ProfileImage from '../../components/ProfileImage';
import MediumButton from '../../components/buttons/MediumButton';
import ScheduleAction, {
  ScheduleActionType,
} from '../calendar/components/ScheduleAction';
import TodoAdd from '../calendar/components/TodoAdd';
import { getScheduleTimeDiff } from '../../utility/time';
import { useTeam } from '../../useTeam';
import useScheduleAction from '../calendar/hooks/useScheduleAction';
import { useUser } from '../../useUser';
import useBlockPop from '../../useBlockPop';

export default function MainPage() {
  const location = useLocation();
  const [banners, setBanners] = useState();
  const [timeDiff, setTimeDiff] = useState();
  const [isTodoAddOpen, toggleTodoAdd] = useReducer((prev) => !prev, false);
  const [isScheduleOpen, toggleSchedule] = useReducer((prev) => !prev, false);

  const { teams, selectedTeam, selectTeam } = useTeam();
  const { userId } = useUser();
  const { data: recentScheduleData, isPending: isMainCardPending } =
    useMainCard(selectedTeam);
  const { data: matesData } = useMainCard2(selectedTeam);
  const { data: notificationsData, markAsRead } = useNotification(selectedTeam);

  // 리렌더링 시 값이 바뀌지 않는 상태 생성
  const date = useRef(new Date()).current;

  const { actionInfo, clearData } = useScheduleAction(date, recentScheduleData);
  const navigate = useNavigate();

  // TODO: 로딩 중 혹은 에러 발생 시 처리

  useBlockPop(location.pathname);

  useEffect(() => {
    clearData();
  }, [isScheduleOpen]);

  useEffect(() => {
    if (notificationsData) {
      setBanners(filterNotifications(notificationsData));
    }
  }, [notificationsData]);

  useEffect(() => {
    if (recentScheduleData) {
      setTimeDiff(getScheduleTimeDiff(recentScheduleData));
    } else {
      setTimeDiff(null);
    }
  }, [recentScheduleData]);

  const getOnMainButtonClick = () => {
    if (teams.length === 0) {
      return () => navigate('/teamspace/make');
    }
    if (!recentScheduleData) {
      return () => toggleSchedule();
    }
    if (timeDiff <= 0) {
      return () =>
        navigate('/feedback/send', {
          state: {
            members: matesData.filter((member) => member.id !== userId),
            scheduleId: recentScheduleData.scheduleId,
            isRegular: true,
          },
        });
    }
    return () => toggleTodoAdd();
  };

  return (
    <div className='flex w-full flex-col'>
      <StickyWrapper className='px-5'>
        {teams && (
          <Accordion
            isMainPage={true}
            selectedTeamId={selectedTeam}
            teamList={teams}
            onTeamClick={selectTeam}
            onClickLastButton={() => navigate('/teamspace/make')}
          />
        )}
      </StickyWrapper>
      {banners && (
        <Slider {...sliderSettings} className='my-4'>
          {banners.notifications.map((banner, index) => (
            <div className='px-[6px]' key={index}>
              <Notification
                notification={banner}
                feedbackRequestNotiIds={banners.feedbackRequestNotiIds}
                onClick={() => console.log('노티 클릭')}
                onClose={markAsRead}
              />
            </div>
          ))}
        </Slider>
      )}
      <div className='h-2' />
      {/* 로컬 스토리지 관련 문제 잡히면 다시 보기 */}
      {timeDiff !== undefined && (
        <MainCard
          userId={userId}
          isInTeam={teams.length > 0}
          recentSchedule={recentScheduleData}
          scheduleDifferece={timeDiff}
          onClickMainButton={getOnMainButtonClick()}
          onClickSubButton={() => toggleSchedule()}
          onClickChevronButton={() => navigate('/calendar')}
        />
      )}
      <div className='h-8' />
      {matesData && (
        <MainCard2
          teamMates={matesData}
          onReceivedFeedbackClick={() =>
            navigate(
              `/feedback/received?teamName=${teams.find((team) => team.id === selectedTeam).name}`,
            )
          }
          onClick={(mate) =>
            showModal(
              <Modal
                type={ModalType.PROFILE}
                profileImage={
                  <div className='size-[62px]'>
                    <ProfileImage
                      iconName={`@animals/${mate.profileImage.image}`}
                      color={mate.profileImage.backgroundColor}
                    />
                  </div>
                }
                content={
                  mate.id === userId ? `${mate.name}(나)` : `${mate.name}님에게`
                }
                mainButton={
                  <MediumButton
                    text={
                      mate.id === userId ? '회고 작성하기' : '피드백 보내기'
                    }
                    onClick={() => {
                      mate.id === userId ?
                        navigate(`/feedback/self`)
                      : navigate(`/feedback/send`, {
                          state: { members: [mate], isRegular: false },
                        });
                      hideModal();
                    }}
                    isOutlined={false}
                    disabled={false}
                  />
                }
                subButton={
                  mate.id === userId ?
                    null
                  : <MediumButton
                      text='피드백 요청하기'
                      onClick={() => {
                        navigate(
                          `/feedback/request?receiverId=${mate.id}&receiverName=${mate.name}`,
                        );

                        hideModal();
                      }}
                      isOutlined={true}
                      disabled={false}
                    />
                }
              />,
            )
          }
        />
      )}
      <div className='h-8' />
      {/* TODO: 달력으로 바꿔야 함 */}
      <ScheduleAction
        type={ScheduleActionType.ADD}
        isOpen={isScheduleOpen}
        onClose={() => {
          toggleSchedule();
        }}
        selectedDateFromParent={new Date()}
        actionInfo={actionInfo}
      />
      {recentScheduleData && (
        <TodoAdd
          isOpen={isTodoAddOpen}
          onSubmit={() => {
            toggleTodoAdd();
            // TODO: 할일 조회
          }}
          onClose={() => {
            toggleTodoAdd();
          }}
          selectedSchedule={recentScheduleData}
        />
      )}
    </div>
  );
}

const sliderSettings = {
  dots: true,
  arrows: false,
  infinite: false,
  speed: 500,
  slidesToShow: 1,
  slidesToScroll: 1,
  centerMode: true,
  centerPadding: '14px',
};
