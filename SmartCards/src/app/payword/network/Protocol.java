package app.payword.network;

public class Protocol
{
	public static class Command
	{
		public static final String sep = " ";
		
		public static final String commandError = "COMMAND-ERROR";
		
		public static final String helloFromUser   = "HELLO-User";
		public static final String helloFromBroker = "HELLO-Broker";
		public static final String helloFromVendor = "HELLO-Vendor";


		public static final String loginRequestCredentials = "LOGIN-Request";
		public static final String loginOfferCredentials   = "LOGIN-Request";
		public static final String loginSuccess = "LOGIN-Success";
		public static final String loginFailure = "LOGIN-Failure";


		public static final String certificateInformationsRequest = "CrtIR";
		public static final String certificateInformationsOffer   = "CrtIO";
		public static final String certificateRequest = "CrtR";
		public static final String certificateOffer   = "CrtO";


		public static final String productsCatalogueRequest = "PCR";
		public static final String productsCatalogueOffer   = "PCO";
		public static final String productReservationRequest  = "PRR";
		public static final String productReservationAccepted = "PRA";
		public static final String productReservationRejected = "PRRj";
		
		/*
		 * ChainRing = PayWord
		 */
		public static final String commitmentRequest  = "CR";
		public static final String commitmentOffer    = "CO";
		public static final String commitmentAccepted = "CA";
		public static final String commitmentRejectedReceiptAborted = "CRRA";
		public static final String commitmentExpiredRequestNewOne   = "CEx-RN";
		public static final String commitmentValueNotEnoughRequestAdditionalOne = "CVNE-RA";
		public static final String commitmentPaywordRequest = "CPR";
		public static final String commitmentPaywordOffer   = "CPO";
		public static final String commitmentPaywordAcceptedReceiptFinalized = "CP-A-RF";
		public static final String commitmentPaywordAcceptedMoreNeeded       = "CP-A-MN";
		public static final String commitmentPaywordRejectedReceiptAborted   = "CP-R-RA";

		
		public static final String receiptRequest     = "RR";
		public static final String receiptOffer       = "RO";
		public static final String receiptAcknowleged = "RA";


		public static final String goodbyeFromUser   = "GOODBYE-User";
		public static final String goodbyeFromBroker = "GOODBYE-Broker";
		public static final String goodbyeFromVendor = "GOODBYE-Vendor";
	}
}
