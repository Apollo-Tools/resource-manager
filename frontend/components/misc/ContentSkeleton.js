import {Skeleton} from 'antd';


const ContentSkeleton = ({isLoading = true, paragraphProps = true, children}) => {
  return (
    <Skeleton className="skeleton" active={false} loading={isLoading} paragraph={paragraphProps}>
      {children}
    </Skeleton>
  );
};

export default ContentSkeleton;
