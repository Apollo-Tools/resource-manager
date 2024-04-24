import {Skeleton} from 'antd';
import PropTypes from 'prop-types';


const ContentSkeleton = ({isLoading = true, titleProps=true, paragraphProps = true, children}) => {
  return (
    <Skeleton className="skeleton" active={false} title={titleProps} loading={isLoading} paragraph={paragraphProps}>
      {children}
    </Skeleton>
  );
};

ContentSkeleton.propTypes = {
  isLoading: PropTypes.bool,
  titleProps: PropTypes.bool,
  paragraphProps: PropTypes.oneOfType([
    PropTypes.shape({row: PropTypes.number, width: PropTypes.number}),
    PropTypes.bool]),
  children: PropTypes.node,
};

export default ContentSkeleton;
