import {CheckCircleTwoTone, CheckOutlined, CloseCircleTwoTone, CloseOutlined} from '@ant-design/icons';
import {ICON_GREEN, ICON_RED} from './Constants';
import PropTypes from 'prop-types';

const BoolValueDisplay = ({value, colored = true, className}) => {
  if (colored) {
    if (value) {
      return <CheckCircleTwoTone twoToneColor={ICON_GREEN} className={className}/>;
    } else {
      return <CloseCircleTwoTone twoToneColor={ICON_RED} className={className}/>;
    }
  } else {
    if (value) {
      return <CheckOutlined className={className}/>;
    } else {
      return <CloseOutlined className={className}/>;
    }
  }
};

BoolValueDisplay.propTypes = {
  value: PropTypes.bool.isRequired,
  colored: PropTypes.bool,
  className: PropTypes.string,
};
export default BoolValueDisplay;
