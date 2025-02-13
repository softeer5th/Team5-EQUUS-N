import Tag, { TagType } from '../../../components/Tag';

/**
 * reports 타입
 * @typedef {object} ReportKeyword
 * @property {string} keyword - 키워드
 * @property {number} count - 개수
 * @property {boolean} isPositive - 긍정적 or 부정적
 */

/**
 * 리포트 키워드 컴포넌트
 * @param {object} props
 * @param {ReportKeyword} props.report - 리포트 키워드
 * @returns {ReactElement} 리포트 키워드 컴포넌트
 */
function Keyword({ report }) {
  return (
    <div className='flex w-full items-center justify-between'>
      <Tag type={TagType.REPORT}>{report.keyword}</Tag>
      <p
        className={`subtitle-2 ml-4 ${report.isPositive ? 'text-blue-300' : 'text-red-300'}`}
      >
        +{report.count}
      </p>
    </div>
  );
}

/**
 * 리포트 키워드 컴포넌트
 * @param {object} props
 * @param {Report[]} props.reports - 리포트 키워드 리스트
 * @returns {ReactElement} 리포트 키워드 컴포넌트
 */
export default function ReportKeywords({ reports }) {
  // TODO: 데이터 형식 추후 확인 필요
  return (
    <div className='rounded-400 flex flex-col gap-3 bg-gray-800 p-5'>
      {reports.map((report, i) => (
        <Keyword key={i} report={report} />
      ))}
    </div>
  );
}
