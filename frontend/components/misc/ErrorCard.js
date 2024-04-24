import {Card} from 'antd';
import {WarningOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';

const ErrorCard = ({isCard= true}) => {
  const errorElement = (
    <>
      <div className="text-center">
        <WarningOutlined className="text-4xl" />
      </div>
      <p className="text-center">
        Error, please try again ...
      </p>
    </>);


  if (isCard) {
    return (
      <Card className="flex justify-center content-center w-96 h-36">
        {errorElement}
      </Card>
    );
  } else {
    return (
      <div className="flex flex-col justify-center content-center">
        {errorElement}
      </div>
    );
  }
};

ErrorCard.propTypes = {
  isCard: PropTypes.bool,
};

export default ErrorCard;
