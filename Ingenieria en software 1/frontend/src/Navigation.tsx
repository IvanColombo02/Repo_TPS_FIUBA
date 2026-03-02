import { Redirect, Route, Switch } from "wouter";

import { EmailVerificationScreen } from "@/screens/EmailVerificationScreen";
import { ResetPasswordScreen } from "@/screens/ResetPasswordScreen";
import { ForgotPasswordScreen } from "@/screens/ForgotPasswordScreen";
import { LoginScreen } from "@/screens/LoginScreen";
import { MainScreen } from "@/screens/MainScreen";
import { ProfileScreen } from "@/screens/ProfileScreen";
import { SignupScreen } from "@/screens/SignupScreen";
import { AdminScreen } from "@/screens/AdminScreen";
import { EmployeeScreen } from "@/screens/EmployeeScreen";
import { MyOrdersScreen } from "@/screens/MyOrdersScreen";
import { useToken } from "@/services/TokenContext";

export const Navigation = () => {
  const [tokenState] = useToken();
  switch (tokenState.state) {
    case "LOGGED_IN":
    case "REFRESHING":
      return (
        <Switch>
          <Route path="/profile">
            <ProfileScreen />
          </Route>
          <Route path="/reset">
            <ResetPasswordScreen />
          </Route>
          <Route path="/orders">
            <MyOrdersScreen />
          </Route>
          <Route path="/admin">
            <AdminScreen />
          </Route>
          <Route path="/employee">
            <EmployeeScreen />
          </Route>
          <Route path="/">
            <MainScreen />
          </Route>
          <Route>
            <Redirect href="/" />
          </Route>
        </Switch>
      );
    case "LOGGED_OUT":
      return (
        <Switch>
          <Route path="/login">
            <LoginScreen />
          </Route>
          <Route path="/signup">
            <SignupScreen />
          </Route>
          <Route path="/verify">
            <EmailVerificationScreen />
          </Route>
          <Route path="/forgot">
            <ForgotPasswordScreen />
          </Route>
          <Route path="/reset">
            <ResetPasswordScreen />
          </Route>
          <Route>
            <Redirect href="/login" />
          </Route>
        </Switch>
      );
    default:
      // Make the compiler check this is unreachable
      return tokenState satisfies never;
  }
};
