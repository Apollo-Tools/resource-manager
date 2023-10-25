import {Typography} from 'antd';
import DataDisplay from './DataDisplay';
import PropTypes from 'prop-types';
import BoolValueDisplay from './BoolValueDisplay';

const {Text} = Typography;

const BoolDataDisplay = ({value, label, className}) => {
  return (
    <DataDisplay label={label} className={className}>
      <Text className="text-start shadow-lg min-w-[250px] max-w-fit p-1 bg-gray-100 block text-gray-600">
        <BoolValueDisplay value={value} className="mr-1"/>
        {value.toString()}
      </Text>
    </DataDisplay>
  );
};

BoolDataDisplay.propTypes = {
  value: PropTypes.node.isRequired,
  label: PropTypes.string.isRequired,
  className: PropTypes.string,
};

export default BoolDataDisplay;
