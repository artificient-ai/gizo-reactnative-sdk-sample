import React, { useState, useEffect, FC } from "react";
import { View, Text, StyleSheet, LogBox } from "react-native";

interface TimerProps {
  isRunning: boolean;
  startTrip: Date;
}

interface TimeState {
  hours: number;
  minutes: number;
  seconds: number;
}

const TimerDisplay: FC<{ value: number; label: string }> = ({ value, label }) => (
  <View style={styles.timerDisplay}>
    <Text style={styles.timeText}>{value.toString().padStart(2, "0")}</Text>
    <Text style={styles.label}>{label}</Text>
  </View>
);

const Timer: FC<TimerProps> = ({ isRunning, startTrip }) => {
  const [time, setTime] = useState<TimeState>({ hours: 0, minutes: 0, seconds: 0 });

  useEffect(() => {
    let interval = null;

    if (isRunning) {
      interval = setInterval(() => {
        const now = new Date();
        const timeDifference = now.getTime() - startTrip.getTime();

        const hours = Math.floor(timeDifference / 3600000);
        const minutes = Math.floor((timeDifference % 3600000) / 60000);
        const seconds = Math.floor((timeDifference % 60000) / 1000);

        setTime({ hours, minutes, seconds });

        const timerMessage = `Updating recording time: ${timeDifference / 1000} seconds`;
        console.log(timerMessage); // Logs to console
      }, 1000);
    } else {
      setTime({ hours: 0, minutes: 0, seconds: 0 }); // Reset timer when stopped
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRunning, startTrip]);

  return (
    <View style={styles.container}>
      <View style={styles.timerContainer}>
        <TimerDisplay value={time.hours} label="Hours" />
        <TimerDisplay value={time.minutes} label="Minutes" />
        <TimerDisplay value={time.seconds} label="Seconds" />
      </View>
    </View>
  );
};

export default Timer;

const styles = StyleSheet.create({
  container: {
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f5f5f5",
    padding: 20,
  },
  timerContainer: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
  },
  timerDisplay: {
    alignItems: "center",
    marginHorizontal: 10,
  },
  timeText: {
    fontSize: 48,
    fontWeight: "bold",
  },
  label: {
    fontSize: 16,
    color: "#666",
  },
});