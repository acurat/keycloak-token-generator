import React from 'react';
import Countdown from 'react-countdown-now';
import { Label } from 'semantic-ui-react';

interface Props {
  value: number;
}

interface RendererProps {
  days: number;
  hours: number;
  minutes: number;
  seconds: number;
  completed: boolean;
}

const formatNumber = (value: number) =>
  value.toLocaleString('en-US', {
    minimumIntegerDigits: 2,
    useGrouping: false,
  });

const Expired = () => <Label color="red">Token Expired</Label>;

const renderer = ({
  days,
  hours,
  minutes,
  seconds,
  completed,
}: RendererProps) => {
  if (completed) {
    return <Expired />;
  }
  return (
    <Label>
      Expires In:
      <Label.Detail>
        {formatNumber(days)}:{formatNumber(hours)}:{formatNumber(minutes)}:
        {formatNumber(seconds)}
      </Label.Detail>
    </Label>
  );
};

const Timer: React.FC<Props> = ({ value }: Props) => (
  <Countdown key={value} date={value * 1000} renderer={renderer} />
);

export default Timer;
