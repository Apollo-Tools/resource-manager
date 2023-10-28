import {CheckCircleTwoTone, CloseCircleTwoTone} from '@ant-design/icons';
import {ICON_GREEN, ICON_RED} from './Constants';
import PropTypes from 'prop-types';

const BoolValueDisplay = ({value, className}) => {
  if (value) {
    return <CheckCircleTwoTone twoToneColor={ICON_GREEN} className={className}/>;
  } else {
    return <CloseCircleTwoTone twoToneColor={ICON_RED} className={className}/>;
  }
};

BoolValueDisplay.propTypes = {
  value: PropTypes.bool.isRequired,
  className: PropTypes.string,
};
export default BoolValueDisplay;
