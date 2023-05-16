import {Typography} from 'antd';
import DataDisplay from './DataDisplay';

const {Text} = Typography;

const TextDataDisplay = ({value, label}) => {
  return (
    <DataDisplay label={label}>
      <Text className="shadow-lg min-w-[250px] max-w-fit p-1 bg-gray-100 block ">
        {value}
      </Text>
    </DataDisplay>
  );
};

export default TextDataDisplay;
