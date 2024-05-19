const data = {
  image: "registry.hub.docker.com/ororura/waves:1.0.0",
  fee: 0,
  imageHash: "b811882c027f86c0827f50a8abd2673020f14f4f0300fbf9cec41c2a6f2f94c2",
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
    const response = await fetch(`http://localhost:6882${url}`, {
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

fetchQuery(data, "/transactions/signAndBroadcast");
