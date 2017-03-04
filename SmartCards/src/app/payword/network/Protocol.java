package app.payword.network;

public class Protocol
{
	public static class Command
	{
		public static final String SEPARATOR = " ";
		
		public static final String commandError = "COMMAND-ERROR";
		
		public static final String helloFromUser   = "HELLO-User";
		public static final String helloFromBroker = "HELLO-Broker";
		public static final String helloFromVendor = "HELLO-Vendor";


		public static final String loginRequestCredentials = "LOGIN-Request";
		public static final String loginOfferCredentials   = "LOGIN-Request";
		public static final String loginSuccess = "LOGIN-Success";
		public static final String loginFailure = "LOGIN-Failure";


		public static final String certificateRequest = "EXCHANGE-PUBLIC-KEY-REQUEST";
		public static final String certificateInformationsRequest    = "adsada2";
		public static final String certificateInformationsOffer   = "adsaasasddada2";
		public static final String certificateOffer = "adsaasdada2";


		public static final String productsCatalogueRequest = "afsPURCHASE-REQUEST";
		public static final String productsCatalogueOffer = "afsPURCHASE-REQUEST";
		public static final String productReservationRequest = "afsPURCasdaHASE-REQUEST";
		public static final String productReservationAccepted = "afsPURCasdaHASE-REQUESTasd";
		public static final String productReservationRejected = "afsPURCasdaHASEasda-REQUESTasd";
		
		/*
		 * ChainRing = PayWord
		 */
		public static final String commitmentRequest = "Commitment-REQUEST";
		public static final String commitmentOffer = "Commitment-OFFER";
		public static final String commitmentAccepted = "Commitment-OFFEadsaR";
		public static final String commitmentRejectedReceiptAborted = "Commitasdadment-OFFEadsaR";
		public static final String commitmentExpiredRequestNewOne = "requestnewOne";
		public static final String commitmentValueNotEnoughRequestAdditionalOne = "asdassda";
		public static final String commitmentChainRingRequest = "dn912n3f8n92";
		public static final String commitmentChainRingOffer = "18hads98";
		public static final String commitmentChainRingAcceptedReceiptFinalized = "123";
		public static final String commitmentChainRingRejectedReceiptAborted = "0ij98h98h8g";

		
		public static final String receiptRequest = "adsa";
		public static final String receiptOffer = "adsa";
		public static final String receiptAcknowleged = "adsasdaa";
		//public static final String receiptFinalized = "adsafa2";
		//public static final String retryLastReceipt = "PURCHASE-REQUEST-Retry";
		//public static final String receiptFinalConfirmationRequest = "PURCHASE-REQUEST";
		public static final String receiptFinalizeRequest = "12PURCHASE-REQUEST";
		// public static final String receiptAborted = "asda";


		public static final String goodbyeFromUser   = "GOODBYE-User";
		public static final String goodbyeFromBroker = "GOODBYE-Broker";
		public static final String goodbyeFromVendor = "GOODBYE-Vendor";
	}
}
