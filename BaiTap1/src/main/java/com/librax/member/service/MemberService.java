package com.librax.member.service;

import com.librax.common.exception.ResourceNotFoundException;
import com.librax.member.dto.MemberRequest;
import com.librax.member.dto.MemberResponse;
import com.librax.member.model.Member;
import com.librax.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int MAX_BORROW_LIMIT = 5;

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả với ID: " + id));
        return mapToResponse(member);
    }

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        Member member = Member.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .currentBorrowedCount(0)
                .build();
        Member saved = memberRepository.save(member);
        return mapToResponse(saved);
    }

    /**
     * Phân tích & Sửa lỗi logic canBorrowBook:
     * Quy định: Độc giả chỉ được mượn tối đa 5 cuốn cùng lúc.
     * Code cũ: (currentBorrowedByMember > 5) -> Nếu độc giả đang mượn 5 cuốn (currentBorrowedByMember = 5),
     * 5 > 5 trả về false, dẫn đến hàm trả về true cho phép mượn thêm cuốn thứ 6!
     * Code đúng: (currentBorrowedByMember >= 5) -> Nếu đã đạt 5 cuốn hoặc hơn, không cho mượn tiếp.
     */
    public boolean canBorrowBook(int currentBorrowedByMember) {
        if (currentBorrowedByMember >= MAX_BORROW_LIMIT) {
            return false;
        }
        return true;
    }

    @Transactional
    public Member getMemberEntityById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy độc giả với ID: " + id));
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .currentBorrowedCount(member.getCurrentBorrowedCount())
                .build();
    }
}
