import {Typography} from 'antd';
import DataDisplay from './DataDisplay';
import PropTypes from 'prop-types';
import ContentSkeleton from './ContentSkeleton';

const {Text} = Typography;

const TextDataDisplay = ({value, label, className, isLoading = false}) => {
  if (isLoading) {
    return (
      <div className={className}>
        <ContentSkeleton paragraphProps={{rows: 1}}/>
      </div>
    );
  }

  return (
    <DataDisplay label={label} className={className}>
      <Text className="shadow-lg min-w-[250px] max-w-fit p-1 bg-gray-100 block text-gray-600">
        {value ?? ''}
      </Text>
    </DataDisplay>
  );
};

TextDataDisplay.propTypes = {
  value: PropTypes.node,
  label: PropTypes.string.isRequired,
  className: PropTypes.string,
  isLoading: PropTypes.bool,
};

export default TextDataDisplay;
