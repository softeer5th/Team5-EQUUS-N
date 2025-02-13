/**
 * 피드백 버튼 컴포넌트
 * @param {object} props
 * @param {string} props.currentFeedback - 현재 선택된 피드백
 * @param {function} props.onClick - 피드백 선택 함수
 * @returns {JSX.Element} - 피드백 버튼 컴포넌트
 */
export default function FeedBackButton({ currentFeedback, onClick }) {
  return (
    <div className='flex gap-3'>
      <div
        className={`rounded-400 flex cursor-pointer flex-col items-center justify-center gap-7 bg-gray-800 px-6.5 py-9 outline-lime-500 select-none ${
          currentFeedback === '칭찬해요' ? 'outline-2' : ''
        }`}
        style={{ width: '170px' }}
        onClick={() => {
          onClick('칭찬해요');
        }}
      >
        <p className='text-4xl'>😀</p>
        <div className='flex flex-col items-center gap-3'>
          <p className='caption-1 text-center text-gray-300'>
            개인의 강점과
            <br />
            행동을 칭찬해주는
          </p>
          <p className='header-4 text-lime-500'>칭찬해요</p>
        </div>
      </div>
      <div
        className={`rounded-400 flex cursor-pointer flex-col items-center justify-center gap-7 bg-gray-800 px-6.5 py-9 outline-lime-500 select-none ${
          currentFeedback === '아쉬워요' ? 'outline-2' : ''
        }`}
        style={{ width: '170px' }}
        onClick={() => {
          onClick('아쉬워요');
        }}
      >
        <p className='text-4xl'>🤔</p>
        <div className='flex flex-col items-center gap-3'>
          <p className='caption-1 text-center text-gray-300'>
            존중하는 말투로
            <br />
            개선 방법을 제공하는
          </p>
          <p className='header-4 text-lime-500'>아쉬워요</p>
        </div>
      </div>
    </div>
  );
}
