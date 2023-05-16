import {Divider} from 'antd';


const SloDisplay = ({slos}) => {
  return <>
    {slos.map((slo) => {
      return (
        <div>
          <div className="grid grid-cols-12 gap-4">
            <span className="shadow-sky-300 shadow-md m-1 bg-sky-800 text-gray-200 w-full rounded-full inline-flex h-6 col-span-2 place-self-center">
              <div className="m-auto font-bold">{slo.name}</div>
            </span>
            <span className="shadow-sky-300 shadow-md m-1 bg-secondary text-gray-200 w-full rounded-full inline-flex w-10 h-7 col-span-1 place-self-center">
              <div className="m-auto font-serif font-extrabold" >{slo.expression}</div>
            </span>
            <span className="m-1 w-full col-span-2 place-self-center">{slo.value.map(((value) => {
              return <div className="shadow-cyan-200 shadow-md m-1 bg-cyan-600 text-gray-200 w-48 rounded-full flex h-6 ">
                <div className="m-auto font-extrabold">
                  {value}
                </div>
              </div>;
            }))}</span>
          </div>
          <Divider className="m-1"/>
        </div>);
    })}
  </>;
};

export default SloDisplay;
