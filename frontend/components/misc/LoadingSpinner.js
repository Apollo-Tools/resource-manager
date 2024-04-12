import {Card, Spin} from 'antd';
import {LoadingOutlined} from '@ant-design/icons';

const LoadingSpinner = () => {
  return <Card className="flex justify-center content-center w-96 h-36">
    <div className="text-center">
      <Spin indicator={<LoadingOutlined className="text-4xl" spin />}/>
    </div>
    <p>
      Loading ...
    </p>
  </Card>;
};

export default LoadingSpinner;
