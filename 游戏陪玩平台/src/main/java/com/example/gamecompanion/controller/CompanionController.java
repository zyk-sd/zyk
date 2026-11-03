package com.example.gamecompanion.controller;

import com.example.gamecompanion.dto.CompanionCard;
import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import com.example.gamecompanion.repository.UserRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.service.CompanionStatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * 陪玩账号管理控制器。
 * 所有登录用户都可以浏览陪玩账号；
 * 只有管理员可以新增、修改、删除陪玩账号。
 */
@Controller
public class CompanionController {

    private final CompanionRepository companionRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final GameOrderRepository orderRepository;
    private final CompanionStatsService companionStatsService;

    public CompanionController(CompanionRepository companionRepository,
                               UserRepository userRepository,
                               ReviewRepository reviewRepository,
                               GameOrderRepository orderRepository,
                               CompanionStatsService companionStatsService) {
        this.companionRepository = companionRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.companionStatsService = companionStatsService;
    }

    /** 陪玩账号列表 */
    @GetMapping("/companions")
    public String list(HttpSession session, Model model) {
        User me = (User) session.getAttribute("loginUser");
        List<CompanionCard> cards = companionStatsService.loadAllCards();
        model.addAttribute("active", "companions");
        model.addAttribute("cards", cards);
        model.addAttribute("isAdmin", me.getRole() == Role.ADMIN);
        model.addAttribute("unboundUsers", unboundCompanionUsers());
        return "companions";
    }

    /** 新增陪玩账号（仅管理员） */
    @PostMapping("/companions/add")
    public String add(@RequestParam("name") String name,
                      @RequestParam("gameName") String gameName,
                      @RequestParam(value = "serverName", required = false) String serverName,
                      @RequestParam(value = "rankLevel", required = false) String rankLevel,
                      @RequestParam("pricePerHour") BigDecimal pricePerHour,
                      @RequestParam(value = "online", defaultValue = "true") Boolean online,
                      @RequestParam(value = "introduction", required = false) String introduction,
                      @RequestParam(value = "userId", required = false) Long userId,
                      HttpSession session,
                      RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "只有管理员可以新增陪玩账号");
            return "redirect:/companions";
        }
        if (name == null || name.isBlank() || gameName == null || gameName.isBlank()) {
            ra.addFlashAttribute("error", "陪玩昵称和游戏名称不能为空");
            return "redirect:/companions";
        }
        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("error", "单价必须大于 0");
            return "redirect:/companions";
        }

        Companion companion = new Companion();
        companion.setName(name.trim());
        companion.setGameName(gameName.trim());
        companion.setServerName(serverName);
        companion.setRankLevel(rankLevel);
        companion.setPricePerHour(pricePerHour);
        companion.setOnline(online);
        companion.setIntroduction(introduction);

        // 绑定陪玩师登录账号（可选）
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != Role.COMPANION) {
                ra.addFlashAttribute("error", "只能绑定角色为“陪玩师”的账号");
                return "redirect:/companions";
            }
            if (companionRepository.existsByUserId(userId)) {
                ra.addFlashAttribute("error", "该账号已经绑定过陪玩资料，不能重复绑定");
                return "redirect:/companions";
            }
            companion.setUser(user);
        }

        companionRepository.save(companion);
        ra.addFlashAttribute("msg", "陪玩账号“" + companion.getName() + "”创建成功");
        return "redirect:/companions";
    }

    /** 打开编辑页 */
    @GetMapping("/companions/edit/{id}")
    public String editPage(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "只有管理员可以编辑陪玩账号");
            return "redirect:/companions";
        }
        Companion companion = companionRepository.findById(id).orElse(null);
        if (companion == null) {
            ra.addFlashAttribute("error", "陪玩账号不存在");
            return "redirect:/companions";
        }
        model.addAttribute("active", "companions");
        model.addAttribute("companion", companion);
        model.addAttribute("card", companionStatsService.loadCard(companion));
        model.addAttribute("unboundUsers", unboundCompanionUsers());
        return "companion-edit";
    }

    /** 保存编辑 */
    @PostMapping("/companions/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam("name") String name,
                         @RequestParam("gameName") String gameName,
                         @RequestParam(value = "serverName", required = false) String serverName,
                         @RequestParam(value = "rankLevel", required = false) String rankLevel,
                         @RequestParam("pricePerHour") BigDecimal pricePerHour,
                         @RequestParam(value = "online", defaultValue = "false") Boolean online,
                         @RequestParam(value = "introduction", required = false) String introduction,
                         @RequestParam(value = "userId", required = false) Long userId,
                         HttpSession session,
                         RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "只有管理员可以编辑陪玩账号");
            return "redirect:/companions";
        }
        Companion companion = companionRepository.findById(id).orElse(null);
        if (companion == null) {
            ra.addFlashAttribute("error", "陪玩账号不存在");
            return "redirect:/companions";
        }
        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            ra.addFlashAttribute("error", "单价必须大于 0");
            return "redirect:/companions/edit/" + id;
        }

        companion.setName(name.trim());
        companion.setGameName(gameName.trim());
        companion.setServerName(serverName);
        companion.setRankLevel(rankLevel);
        companion.setPricePerHour(pricePerHour);
        companion.setOnline(online);
        companion.setIntroduction(introduction);

        Long currentBindId = companion.getUser() == null ? null : companion.getUser().getId();
        if (userId != null && !userId.equals(currentBindId)) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getRole() != Role.COMPANION) {
                ra.addFlashAttribute("error", "只能绑定角色为“陪玩师”的账号");
                return "redirect:/companions/edit/" + id;
            }
            if (companionRepository.existsByUserId(userId)) {
                ra.addFlashAttribute("error", "该账号已经绑定过其他陪玩资料");
                return "redirect:/companions/edit/" + id;
            }
            companion.setUser(user);
        }

        companionRepository.save(companion);
        ra.addFlashAttribute("msg", "陪玩账号“" + companion.getName() + "”已更新");
        return "redirect:/companions";
    }

    /** 删除陪玩账号（仅管理员，且必须没有订单记录） */
    @PostMapping("/companions/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "只有管理员可以删除陪玩账号");
            return "redirect:/companions";
        }
        Companion companion = companionRepository.findById(id).orElse(null);
        if (companion == null) {
            ra.addFlashAttribute("error", "陪玩账号不存在");
            return "redirect:/companions";
        }
        if (orderRepository.existsByCompanionId(id)) {
            ra.addFlashAttribute("error", "该陪玩账号已有订单记录，不能删除（可改为下线）");
            return "redirect:/companions";
        }
        if (!reviewRepository.findByCompanionIdOrderByCreateTimeDesc(id).isEmpty()) {
            ra.addFlashAttribute("error", "该陪玩账号已有评价记录，不能删除");
            return "redirect:/companions";
        }
        companionRepository.delete(companion);
        ra.addFlashAttribute("msg", "陪玩账号“" + companion.getName() + "”已删除");
        return "redirect:/companions";
    }

    /** 快捷切换上线 / 下线状态 */
    @PostMapping("/companions/toggle/{id}")
    public String toggle(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!isAdmin(session)) {
            ra.addFlashAttribute("error", "只有管理员可以修改接单状态");
            return "redirect:/companions";
        }
        Companion companion = companionRepository.findById(id).orElse(null);
        if (companion == null) {
            ra.addFlashAttribute("error", "陪玩账号不存在");
            return "redirect:/companions";
        }
        companion.setOnline(!Boolean.TRUE.equals(companion.getOnline()));
        companionRepository.save(companion);
        ra.addFlashAttribute("msg", "“" + companion.getName() + "”已切换为" +
                (Boolean.TRUE.equals(companion.getOnline()) ? "在线接单" : "下线休息"));
        return "redirect:/companions";
    }

    /** 查询尚未绑定陪玩资料的陪玩师账号 */
    private List<User> unboundCompanionUsers() {
        return userRepository.findByRoleOrderByIdAsc(Role.COMPANION).stream()
                .filter(u -> !companionRepository.existsByUserId(u.getId()))
                .toList();
    }

    private boolean isAdmin(HttpSession session) {
        User me = (User) session.getAttribute("loginUser");
        return me != null && me.getRole() == Role.ADMIN;
    }
}
