import Tag from '../../../components/Tag';
import { TagType } from '../../../components/Tag';
import Icon from '../../../components/Icon';
import MediumButton from '../../../components/buttons/MediumButton';
import { useEffect, useRef, useState } from 'react';
import { changeDayName } from '../../../utility/time';

/**
 * 메인 카드 컴포넌트
 * @param {object} props
 * @param {number} props.userId - 사용자 ID
 * @param {boolean} props.isInTeam - 팀에 속해있는지 여부
 * @param {Schedule} props.recentSchedule - 최신 스케줄
 * @param {number} props.scheduleDifferece - D-day
 * @param {function} props.onClickMainButton - 메인 버튼 클릭 이벤트
 * @param {function} props.onClickSubButton - 서브 버튼 클릭 이벤트
 * @param {function} props.onClickChevronButton - 체크론 버튼 클릭 이벤트
 * @returns {ReactElement}
 */
export default function MainCard({
  userId,
  isInTeam,
  recentSchedule,
  scheduleDifferece,
  onClickMainButton,
  onClickSubButton,
  onClickChevronButton,
}) {
  console.log(userId, isInTeam, recentSchedule, scheduleDifferece);
  const contentRef = useRef(null);
  const [isOpen, setIsOpen] = useState(false);
  const [height, setHeight] = useState(0);
  // 스케줄 컨텐츠 높이 계산
  useEffect(() => {
    if (contentRef.current) {
      setHeight(isOpen ? contentRef.current.scrollHeight : 0);
    }
  }, [isOpen]);

  if (!isInTeam) {
    // 팀에 속하지 않은 경우 무조건 팀 만들어야 함
    return (
      <MainCardFrame onClickChevronButton={onClickChevronButton}>
        <p className='body-1 mt-11 mb-10 text-center text-gray-300'>
          팀 스페이스가 비어있어요
        </p>
        <MediumButton
          text={'팀 스페이스 생성하기'}
          isOutlined={false}
          onClick={onClickMainButton}
        />
      </MainCardFrame>
    );
  }

  // 마지막 일정이 끝난지 24시간이 넘은 경우 -> response 자체가 없음
  if (scheduleDifferece == null) {
    return (
      <MainCardFrame onClickChevronButton={onClickChevronButton}>
        <p className='body-1 mt-11 mb-10 text-center text-gray-300'>
          다음 일정이 비어있어요
        </p>
        <MediumButton
          text={'일정 추가하기'}
          isOutlined={false}
          onClick={onClickMainButton}
        />
      </MainCardFrame>
    );
  }

  // 마지막 일정이 끝난 후 24시간이 안된 경우: response 있음 && timeDiff가 0보다 작거나 같음
  if (scheduleDifferece <= 0) {
    return (
      <MainCardFrame onClickChevronButton={onClickChevronButton}>
        <div className='flex flex-col items-center justify-center'>
          <p className='body-1 mt-6 mb-1 text-gray-300'>일정 종료</p>
          <h1 className='header-1 mb-7 text-gray-400'>
            {recentSchedule.scheduleName}
          </h1>
          <MediumButton
            text={'피드백 작성하기'}
            isOutlined={false}
            onClick={onClickMainButton}
          />
          <div className='h-3' />
          <MediumButton
            text={'다음 일정 추가하기'}
            isOutlined={true}
            disabled={true}
            onClick={onClickSubButton}
          />
        </div>
      </MainCardFrame>
    );
  } else {
    // 다음 일정
    return (
      <MainCardFrame onClickChevronButton={onClickChevronButton}>
        {renderTitle(recentSchedule, scheduleDifferece)}
        <hr className='my-6 w-full border-gray-500' />
        {renderMyRole(recentSchedule, onClickMainButton, userId)}
        {renderTeamRole(recentSchedule, contentRef, height, userId)}
        <MediumButton
          text={
            <div className='flex items-center gap-2'>
              <Icon
                className={isOpen ? 'rotate-180' : null}
                name={'chevronDown'}
              />
              {isOpen ? '닫기' : '팀원 역할 보기'}
            </div>
          }
          onClick={() => setIsOpen(!isOpen)}
          isOutlined={true}
          disabled={true}
        />
      </MainCardFrame>
    );
  }
}

function MainCardFrame({ children, onClickChevronButton }) {
  return (
    <div className={`rounded-400 relative mx-5 flex flex-col bg-gray-800 p-4`}>
      {children}
      <button onClick={onClickChevronButton}>
        <Icon name='chevronRight' className='absolute top-4 right-4' />
      </button>
    </div>
  );
}

/**
 * 메인 카드 타이틀 렌더링
 * @param {Schedule} recentSchedule
 * @param {string} scheduleDifferece
 * @returns
 */
function renderTitle(recentSchedule, scheduleDifferece) {
  const dateObject = new Date(recentSchedule.startTime);
  return (
    <div className='flex flex-col items-center justify-center pt-6'>
      <p className='body-1 text-gray-300'>
        {'다음 일정까지'}
        <span className='body-4 ml-2 text-lime-200'>{`D-${scheduleDifferece}`}</span>
      </p>
      <h1 className='header-1 my-2 text-gray-100'>
        {recentSchedule.scheduleName}
      </h1>
      <Tag
        type={TagType.TEAM_SCHEDULE}
        children={{
          date: `${dateObject.getDate()}일 ${changeDayName(dateObject.getDay())}요일`,
          time: `${dateObject.getHours()}:${dateObject.getMinutes()}`,
        }}
      />
    </div>
  );
}

/**
 * 나의 역할 렌더링
 * @param {Schedule} recentSchedule
 * @param {function} onButtonClick
 * @param {number} userId
 */
function renderMyRole(recentSchedule, onButtonClick, userId) {
  return (
      recentSchedule.scheduleMemberNestedDtoList.find(
        (role) => role.memberId === userId,
      ).todoList[0] !== null
    ) ?
      <div className='flex flex-col gap-3'>
        <Tag type={TagType.MY_ROLE} />
        <ul className='flex list-disc flex-col gap-1 pl-6'>
          {recentSchedule.scheduleMemberNestedDtoList
            .find((role) => role.memberId === userId)
            .todoList.map((role, index) => (
              <li key={index} className='body-1 pl-1 text-gray-100 last:mb-3'>
                {role}
              </li>
            ))}
        </ul>
      </div>
    : <div className='mb-3 flex flex-col gap-6'>
        <p className='body-1 text-center text-gray-400'>
          나의 역할이 비어있어요
        </p>
        <MediumButton
          text={'나의 역할 추가하기'}
          isOutlined={false}
          onClick={onButtonClick}
        />
      </div>;
}

/**
 * 팀원 역할 렌더링
 * @param {Schedule} recentSchedule
 * @param {object} contentRef
 * @param {number} currentHeight
 * @param {number} userId
 */
function renderTeamRole(recentSchedule, contentRef, currentHeight, userId) {
  const filteredTeamTodos = recentSchedule.scheduleMemberNestedDtoList.filter(
    (todo) => todo.memberId !== userId,
  );
  return (
    <ul
      ref={contentRef}
      className={`flex flex-col overflow-hidden transition-all duration-300 ease-in-out`}
      style={
        contentRef.current ? { height: `${currentHeight}px` } : { height: 0 }
      }
    >
      {filteredTeamTodos.length > 0 ?
        filteredTeamTodos.map((role, index) => (
          <li key={index} className='flex flex-col gap-3 first:mt-3'>
            <Tag type={TagType.MEMBER_ROLE}>{role.memberName}</Tag>
            {role.todoList[0] === null ?
              <p className='body-1 mb-2 ml-1 text-gray-500'>
                아직 담당 업무를 입력하지 않았어요
              </p>
            : <ul className='flex list-disc flex-col gap-1 pl-6'>
                {role.todoList.map((todo, index) => (
                  <li
                    key={index}
                    className='body-1 pl-1 text-gray-100 last:mb-6'
                  >
                    {todo}
                  </li>
                ))}
              </ul>
            }
          </li>
        ))
      : <div className='body-1 mt-2 mb-4 text-center text-gray-400'>
          팀원이 존재하지 않아요
        </div>
      }
    </ul>
  );
}
