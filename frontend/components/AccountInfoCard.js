import DateFormatter from './DateFormatter';
import ResetPasswordForm from './ResetPasswordForm';
import {Typography} from 'antd';
import PropTypes from 'prop-types';

const {Title} = Typography;

const AccountInfoCard = ({account}) => {
  if (!account) {
    return;
  }
  return (
    <div className="flex md:flex-row flex-col">
      <div className="basis-full md:basis-1/2">
        <Title level={5} className="mt-0.5">Username</Title>
        {account.username}
        <Title level={5}>Created At</Title>
        <DateFormatter dateTimestamp={account.created_at} />
      </div>
      <div className="basis-full md:basis-1/2">
        <Title level={5} className="mt-0.5">Reset Password</Title>
        <ResetPasswordForm />
      </div>
    </div>
  );
};

AccountInfoCard.propTypes = {
  account: PropTypes.object,
};

export default AccountInfoCard;
