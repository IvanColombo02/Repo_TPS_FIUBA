import { Login } from "@/components/Login/Login";
import { useLogin } from "@/services/UserServices";

export const LoginScreen = () => {
  const { mutate, error } = useLogin();
  return (
    <Login onSubmit={mutate} submitError={error} />
  );
};
