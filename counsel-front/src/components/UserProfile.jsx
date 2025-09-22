import React, { useEffect, useState } from "react";
import { getUserProfile, updateUserProfile } from "../api/userApi";

const UserProfile = ({ userId }) => {
  const [profile, setProfile] = useState({
    성별: "",
    나이: "",
    관심사: "",
    고민: ""
  });
  const [editMode, setEditMode] = useState(false);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await getUserProfile(userId);

        // 백엔드에서 gender가 그냥 문자열이므로 그대로 사용
        setProfile({
          성별: data.gender || "",
          나이: data.age || "",
          관심사: data.interests || "",
          고민: data.concern || ""
        });
      } catch (err) {
        console.error(err.response?.data || err.message);
        alert("프로필을 불러오는 중 오류가 발생했습니다.");
      }
    };

    fetchProfile();
  }, [userId]);

  const handleChange = (e) => {
    const { name, value } = e.target;

    // 나이 음수 방지
    if (name === "나이" && Number(value) < 0) return;

    setProfile({ ...profile, [name]: value });
  };

  const handleUpdate = async () => {
    try {
      const updateData = {
        gender: profile.성별,       // 그대로 보내기
        age: Number(profile.나이),  // 숫자로 변환
        interests: profile.관심사,
        concern: profile.고민
      };

      const res = await updateUserProfile(userId, updateData);
      console.log(res); // 서버 응답 확인
      setEditMode(false);
      alert("프로필이 업데이트되었습니다!");
    } catch (err) {
      console.error(err.response?.data || err.message);
      alert("저장 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="card bg-base-100 shadow-md p-6 mt-4">
      <h2 className="text-xl font-bold mb-4">내 프로필</h2>

      {editMode ? (
        <div className="space-y-3">
          {/* 성별 라디오 */}
          <div className="flex items-center gap-4">
            <label className="flex items-center gap-1">
              <input
                type="radio"
                name="성별"
                value="남"
                checked={profile.성별 === "남"}
                onChange={handleChange}
                className="radio radio-primary"
              />
              남
            </label>
            <label className="flex items-center gap-1">
              <input
                type="radio"
                name="성별"
                value="여"
                checked={profile.성별 === "여"}
                onChange={handleChange}
                className="radio radio-primary"
              />
              여
            </label>
          </div>

          {/* 나이 */}
          <input
            name="나이"
            type="number"
            value={profile.나이}
            onChange={handleChange}
            placeholder="나이"
            className="input input-bordered w-full"
            min="0"
          />

          {/* 관심사 */}
          <input
            name="관심사"
            value={profile.관심사}
            onChange={handleChange}
            placeholder="관심사"
            className="input input-bordered w-full"
          />

          {/* 고민 */}
          <input
            name="고민"
            value={profile.고민}
            onChange={handleChange}
            placeholder="고민"
            className="input input-bordered w-full"
          />

          <button className="btn btn-primary mt-2" onClick={handleUpdate}>
            저장
          </button>
        </div>
      ) : (
        <div className="space-y-2">
          <p>성별: {profile.성별}</p>
          <p>나이: {profile.나이}</p>
          <p>관심사: {profile.관심사}</p>
          <p>고민: {profile.고민}</p>
          <button className="btn btn-secondary mt-2" onClick={() => setEditMode(true)}>
            수정
          </button>
        </div>
      )}
    </div>
  );
};

export default UserProfile;
