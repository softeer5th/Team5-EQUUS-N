import { useLocation, useNavigate } from 'react-router-dom';
import KeywordButton from '../../components/buttons/KeywordButton';
import { useEditFavorite, useFeedbackFavorite } from '../../api/useFeedback';
import { useState } from 'react';
import FooterWrapper from '../../components/wrappers/FooterWrapper';
import LargeButton from '../../components/buttons/LargeButton';
import { useSignUp } from '../../api/useAuth';
import { showToast } from '../../utility/handleToast';
import { useUser } from '../../useUser';
import StickyWrapper from '../../components/wrappers/StickyWrapper';
import NavBar2 from '../../components/NavBar2';

export default function FeedbackFavorite() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setUserId } = useUser();
  const process = new URLSearchParams(location.search).get('process');

  const [selectedStyle, setSelectedStyle] = useState([]);
  const [selectedContent, setSelectedContent] = useState([]);

  const { data } = useFeedbackFavorite();

  const mutation = process === 'signup' ? useSignUp() : useEditFavorite();

  const onKeywordButtonClick = (isStyle, keyword) => {
    const keywords = isStyle ? selectedStyle : selectedContent;
    const setKeywords = isStyle ? setSelectedStyle : setSelectedContent;
    keywords.includes(keyword) ?
      setKeywords([...keywords.filter((item) => item !== keyword)])
    : keywords.length < 2 && setKeywords([...keywords, keyword]);
  };

  const onFinish = () => {
    selectedStyle.length === 0 || selectedContent.length === 0 ?
      showToast('피드백 유형을 최소 한 개씩 선택해주세요')
    : mutation.mutate(
        {
          ...location.state,
          feedbackPreferences: [...selectedStyle, ...selectedContent],
        },
        {
          onSuccess: (data) => {
            if (process === 'signup') {
              const { message, id } = data;
              setUserId(id);
              showToast(message);
              navigate('/teamspace/make/first');
            } else {
              navigate('/mypage');
              showToast('수정 완료');
            }
          },
        },
      );
  };

  return (
    <div className='flex size-full flex-col'>
      {process === 'signup' ?
        <h1 className='header-2 text-gray-0 pt-10 pb-3 whitespace-pre-line'>
          {'선호하는 피드백 유형을\n선택해 주세요'}
        </h1>
      : <StickyWrapper>
          <NavBar2
            canPop={true}
            title='선호 피드백 유형 관리'
            onClickPop={() => {
              navigate(-1);
            }}
          />
        </StickyWrapper>
      }
      <p className={`body-1 ${process !== 'signup' && 'mt-5'} text-gray-200`}>
        카테고리별 최대 2개까지 선택해 주세요
      </p>
      {data && (
        <div className='mt-10 flex flex-col'>
          <h2 className='subtitle-1 text-gray-0 mb-3'>스타일</h2>
          <div className='flex flex-wrap gap-2'>
            {data['스타일'].map((keyword, index) => (
              <KeywordButton
                key={index}
                isActive={selectedStyle.includes(keyword)}
                onClick={() => onKeywordButtonClick(true, keyword)}
              >
                {keyword}
              </KeywordButton>
            ))}
          </div>
          <div className='h-8' />
          <h2 className='subtitle-1 text-gray-0 mb-3'>내용</h2>
          <div className='flex flex-wrap gap-2'>
            {data['내용'].map((keyword, index) => (
              <KeywordButton
                key={index}
                isActive={selectedContent.includes(keyword)}
                onClick={() => onKeywordButtonClick(false, keyword)}
              >
                {keyword}
              </KeywordButton>
            ))}
          </div>
        </div>
      )}
      <FooterWrapper>
        <LargeButton
          isOutlined={false}
          text={mutation.isPending ? '로딩중' : '완료'} // 로딩 중일 때 버튼 텍스트 변경... 추후 수정 필요
          disabled={selectedStyle.length === 0 && selectedContent.length === 0}
          onClick={onFinish}
        />
      </FooterWrapper>
    </div>
  );
}
