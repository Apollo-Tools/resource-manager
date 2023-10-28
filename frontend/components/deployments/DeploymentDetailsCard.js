import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import {computeTimeDifference} from '../../lib/misc/DateTimeCompareService';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';


const DeploymentDetailsCard = ({deployment}) => {
  const [isFinished, setFinished] = useState(false);

  useEffect(() => {
    setFinished(Object.hasOwn(deployment, 'finished_at') && deployment.finished_at != null);
  }, [deployment]);

  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
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
