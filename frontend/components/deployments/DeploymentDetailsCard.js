import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import {computeTimeDifference} from '../../lib/misc/DateTimeCompareService';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {CopyOutlined} from '@ant-design/icons';
import {Button} from 'antd';


const DeploymentDetailsCard = ({deployment}) => {
  const [isFinished, setFinished] = useState(false);

  useEffect(() => {
    setFinished(Object.hasOwn(deployment, 'finished_at') && deployment.finished_at != null);
  }, [deployment]);

  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      {deployment.alert_notification_url && <TextDataDisplay
        label="Alertin URL"
        value={<>
          {deployment.alert_notification_url}
          <Button className="text-gray-400 ml-1.5" type="ghost" icon={<CopyOutlined />}
            onClick={async () => {
              await navigator.clipboard.writeText(deployment.alert_notification_url);
            }}/></>}
        className="col-span-6" />}
      <TextDataDisplay
        label="Created at"
        value={<DateFormatter dateTimestamp={deployment.created_at} includeTime/>} className="col-span-6"/>
      <TextDataDisplay
        label="Finished at"
        value={isFinished ? <DateFormatter dateTimestamp={deployment.finished_at} includeTime/> : 'not finished'}
        className="col-span-6"/>
      <TextDataDisplay
        label="Total deployment time"
        value={isFinished ? computeTimeDifference(deployment.created_at, deployment.finished_at) : 'not finished'}
        className="col-span-6"/>
    </div>
  );
};

DeploymentDetailsCard.propTypes = {
  deployment: PropTypes.object.isRequired,
};

export default DeploymentDetailsCard;
