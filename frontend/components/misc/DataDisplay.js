import {Typography} from 'antd';

const {Title} = Typography;

const DataDisplay = ({label, children}) => {
  return (
    <div>
      <Title level={5} className="mt-0.5 mb-2">{label}</Title>
      {children}
    </div>
  );
};

export default DataDisplay;
