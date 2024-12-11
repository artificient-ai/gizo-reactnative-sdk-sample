import React, { useState, useEffect, FC } from "react";
import { View, Text, StyleSheet } from "react-native";

interface TimerProps {
  isRunning: boolean;
}

// Timer Display Component
const TimerDisplay = ({ value, label }) => (
  <View style={styles.timerDisplay}>
    <Text style={styles.timeText}>{value.toString().padStart(2, "0")}</Text>
    <Text style={styles.label}>{label}</Text>
  </View>
);

// Main Timer Component
const Timer: FC<TimerProps> = ({ isRunning }) => {
  const [time, setTime] = useState({ hours: 0, minutes: 0, seconds: 0 });

  useEffect(() => {
    let interval = null;

    if (isRunning) {
      interval = setInterval(() => {
        setTime((prevTime) => {
          const totalSeconds =
            prevTime.hours * 3600 +
            prevTime.minutes * 60 +
            prevTime.seconds +
            1;
          const hours = Math.floor(totalSeconds / 3600);
          const minutes = Math.floor((totalSeconds % 3600) / 60);
          const seconds = totalSeconds % 60;
          return { hours, minutes, seconds };
        });
      }, 1000);
    } else {
      if (interval) clearInterval(interval!);
      setTime({ hours: 0, minutes: 0, seconds: 0 }); // Reset the timer
    }

    return () => {
      if (interval) clearInterval(interval);
    };
  }, [isRunning]);

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

// Styles
const styles = StyleSheet.create({
  container: {
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#f5f5f5",
  },
  timerContainer: {
    flexDirection: "row",
    justifyContent: "center",
    alignItems: "center",
    marginBottom: 20,
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
  buttonContainer: {
    flexDirection: "row",
    justifyContent: "center",
  },
  button: {
    padding: 15,
    borderRadius: 10,
    marginHorizontal: 10,
  },
  startButton: {
    backgroundColor: "#4CAF50",
  },
  stopButton: {
    backgroundColor: "#F44336",
  },
  buttonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "bold",
  },
});
