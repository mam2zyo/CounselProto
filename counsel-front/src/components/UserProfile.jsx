// src/components/UserProfile.jsx
import { useEffect, useState } from "react";
import { getUserProfile, updateUserProfile } from "../api/user";
import { useAuth } from "../contexts/AuthContext";

const UserProfile = () => {
  const { user } = useAuth();
  const userId = user?.id;

  // 프론트 state는 기존 UI 이름 그대로
  const [profile, setProfile] = useState({
    성별: "",
    나이: "",
    관심사: "",
    고민: "",
    결제종료일: "",
  });
  const [editMode, setEditMode] = useState(false);
  const [loading, setLoading] = useState(true);

  // 프로필 조회
  const fetchProfile = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const data = await getUserProfile(userId);

      setProfile({
        성별: data.gender || "선택안함",
        나이: data.age || "",
        관심사: data.interests || "",
        고민: data.concern || "",
        결제종료일: data.accessUntil
          ? new Date(data.accessUntil).toLocaleDateString()
          : "없음",
      });
    } catch (err) {
      console.error("프로필 로딩 실패", err);
      alert("프로필 불러오기 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, [userId]);

  if (!userId) return <div>로그인 후 이용 가능합니다.</div>;
  if (loading) return <div>로딩 중...</div>;

  // 저장 처리
  const handleSave = async () => {
    try {
      const updateData = {
        gender: profile.성별,
        age: Number(profile.나이),
        interests: profile.관심사,
        concern: profile.고민,
      };

      await updateUserProfile(userId, updateData);

      // 최신 데이터 다시 가져오기
      await fetchProfile();

      setEditMode(false);
      alert("프로필이 업데이트되었습니다!");
    } catch (err) {
      console.error("프로필 저장 실패", err);
      alert("저장 실패");
    }
  };

  // 값 변경 처리
  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === "나이" && Number(value) < 0) return;
    setProfile({ ...profile, [name]: value });
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

          <button className="btn btn-primary mt-2" onClick={handleSave}>
            저장
          </button>
          <button
            className="btn btn-secondary mt-2 ml-2"
            onClick={() => setEditMode(false)}
          >
            취소
          </button>
        </div>
      ) : (
        <div className="space-y-2">
          <p>성별: {profile.성별}</p>
          <p>나이: {profile.나이}</p>
          <p>관심사: {profile.관심사}</p>
          <p>고민: {profile.고민}</p>
          <p>결제 종료일: {profile.결제종료일}</p>

          <button
            className="btn btn-secondary mt-2"
            onClick={() => setEditMode(true)}
          >
            수정
          </button>
        </div>
      )}
    </div>
  );
};

export default UserProfile;
