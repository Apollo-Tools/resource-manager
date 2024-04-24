import {Typography} from 'antd';
import DataDisplay from './DataDisplay';
import PropTypes from 'prop-types';
import BoolValueDisplay from './BoolValueDisplay';
import ContentSkeleton from './ContentSkeleton';

const {Text} = Typography;

const BoolDataDisplay = ({value, label, isLoading = false, className}) => {
  if (isLoading) {
    return (
      <div className="min-w-[250px]">
        <ContentSkeleton paragraphProps={{rows: 1}}/>
      </div>
    );
  }

  return (
    <DataDisplay label={label} className={className}>
      <Text className="text-start shadow-lg min-w-[250px] max-w-fit p-1 bg-gray-100 block text-gray-600">
        <BoolValueDisplay value={value} className="mr-1"/>
        {value?.toString() ?? ''}
      </Text>
    </DataDisplay>
  );
};

BoolDataDisplay.propTypes = {
  value: PropTypes.bool,
  label: PropTypes.string.isRequired,
  isLoading: PropTypes.bool,
  className: PropTypes.string,
};

export default BoolDataDisplay;
