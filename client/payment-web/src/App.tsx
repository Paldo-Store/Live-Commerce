import { PaymentApprovePage } from "./pages/PaymentApprovePage";
import { PaymentCancelPage } from "./pages/PaymentCancelPage";
import { PaymentFailPage } from "./pages/PaymentFailPage";
import { PaymentStartPage } from "./pages/PaymentStartPage";

function App() {
  const path = window.location.pathname;

  if (path === "/payments/approve") {
    return <PaymentApprovePage />;
  }

  if (path === "/payments/cancel") {
    return <PaymentCancelPage />;
  }

  if (path === "/payments/fail") {
    return <PaymentFailPage />;
  }

  return <PaymentStartPage />;
}

export default App;
