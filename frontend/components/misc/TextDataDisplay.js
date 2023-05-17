import {Typography} from 'antd';
import DataDisplay from './DataDisplay';
import PropTypes from 'prop-types';

const {Text} = Typography;

const TextDataDisplay = ({value, label, className}) => {
  return (
    <DataDisplay label={label} className={className}>
      <Text className="shadow-lg min-w-[250px] max-w-fit p-1 bg-gray-100 block text-gray-600">
        {value}
      </Text>
    </DataDisplay>
  );
};

TextDataDisplay.propTypes = {
  value: PropTypes.node.isRequired,
  label: PropTypes.string.isRequired,
  className: PropTypes.string,
};

export default TextDataDisplay;
