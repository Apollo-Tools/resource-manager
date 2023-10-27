import {Typography} from 'antd';
import PropTypes from 'prop-types';


const NothingToSelectCard = ({text}) => {
  return (
    <Typography.Title
      level={3}
      className="p-6 text-center bg-cyan-50 rounded-md shadow-lg"
    >
      {text}
    </Typography.Title>
  );
};

NothingToSelectCard.propTypes = {
  text: PropTypes.string.isRequired,
};

export default NothingToSelectCard;
