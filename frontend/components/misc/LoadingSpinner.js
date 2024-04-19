import {Card, Spin} from 'antd';
import {LoadingOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

const LoadingSpinner = ({isCard= true}) => {
  const loadingElement = (
    <>
      <div className="text-center">
        <Spin indicator={<LoadingOutlined className="text-4xl" spin/>}/>
      </div>
      <p className="text-center">
        Please wait ...
      </p>
    </>);


  if (isCard) {
    return (
      <Card className="flex justify-center content-center w-96 h-36">
        {loadingElement}
      </Card>
    );
  } else {
    return (
      <div className="flex flex-col justify-center content-center">
        {loadingElement}
      </div>
    );
  }
};

LoadingSpinner.propTypes = {
  isCard: PropTypes.bool,
};

export default LoadingSpinner;
