const data = {
  image: "registry.hub.docker.com/ororura/waves:1.0.0",
  fee: 0,
  imageHash: "c79b9148ae1e916aba31695507e9230ab55ace56e90ca3cc7be31270a25aa34b",
  type: 103,
  params: [
    {
      type: "string",
      value: "init",
      key: "action",
    },
  ],
  version: 2,
  sender: "3Nh6Xwbpf2UWEm1qZSnF5aSse2mTXpsBt98",
  password: "umQlMdslhWHLCQX-5qfJxQ",
  feeAssetId: null,
  contractName: "myAwesomeContract",
};

const newUser = {
  contractId: "G54xPfyT11LxDtYSbhn2wz9qD96jRZpb2B2bKAxvKFLn",
  fee: 0,
  sender: "3NfmeYrmFPYyP4ju45pMHYMnQLMo9CtdXvP",
  password: "D0qhiJiuAlIe3X_HjfA-oQ",
  type: 104,
  params: [
    {
      type: "string",
      value: "createUser",
      key: "action",
    },
    {
      type: "string",
      value: `{"name": "Test", "homeAddress": "0129378testtest", "blockchainAddress": "3NfmeYrmFPYyP4ju45pMHYMnQLMo9CtdXvP", "balance": "200"}`,
      key: "user",
    },
  ],
  version: 2,
  contractVersion: 1,
};

const fetchQuery = async (data, url) => {
  try {
    const response = await fetch(`http://localhost:6862${url}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json;charset=utf-8",
      },
      body: JSON.stringify(data),
    });

    await console.log(response);
  } catch (e) {
    console.log(e);
  }
};

//fetchQuery(data, "/transactions/signAndBroadcast");

fetchQuery(newUser, "/transactions/signAndBroadcast");
