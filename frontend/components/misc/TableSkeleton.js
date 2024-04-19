import {Skeleton} from 'antd';


const TableSkeleton = ({isLoading, children}) => {
  return <Skeleton className="skeleton" active={false} />;
};

export default TableSkeleton;
