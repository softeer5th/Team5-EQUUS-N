/**
 * @param {object} props
 * @param {string} props.title
 * @returns
 */
export default function NavBar({ title }) {
  return (
    <div className='h-fit w-full pt-10 pb-3'>
      <h1 className='header-2 text-gray-0 text-left'>{title}</h1>
    </div>
  );
}
